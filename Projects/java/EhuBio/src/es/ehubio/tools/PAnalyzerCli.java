package es.ehubio.tools;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.io.Mzid;
import es.ehubio.proteomics.io.SpHppCsv;
import es.ehubio.proteomics.pipeline.Filter;
import es.ehubio.proteomics.pipeline.PAnalyzer;
import es.ehubio.proteomics.pipeline.Validator;

public final class PAnalyzerCli implements Command.Interface {
	private static final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());
	private MsMsData data;
	private MsMsFile file;
	private PAnalyzer pAnalyzer;
	private Validator validator;
	private final ScoreType scoreType = ScoreType.XTANDEM_EVALUE;
	private final double fdr = 0.01;
	private final static int MAXITER=15;	
	
	@XmlRootElement
	private static class Configuration {
		public static class InputFile {
			public String path;
			public String decoyRegex;
		}
		public String description;
		public String operation;
		@XmlElement(name="input")
		public List<InputFile> inputs;
		public String output;
	}

	@Override
	public String getUsage() {
		return "</path/experiment.pax>";
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}	

	@Override
	public void run(String[] args) throws Exception {
		Configuration cfg = load(args[0]);		
		initialize();
		rebuildGroups();		
		inputFilter();
		switch( cfg.operation ) {
			case "psm":
				logger.info("--- PSM FDR based process ---");
				processPsmFdr();
				break;
			case "pep":
				logger.info("--- Peptide FDR based process ---");
				processPeptideFdr();
				break;
			case "grp":
				logger.info("--- Protein group FDR based process (iterative) ---");
				processGroupFdr();
				break;
			default:
				logger.info("--- Non FDR based process ---");
		}
		finalSteps();
		save(cfg);
		
		logger.info("finished!");
	}	

	private void initialize() {
		pAnalyzer = new PAnalyzer(data);		
		validator = new Validator(data);
	}

	private void processPsmFdr() {				
		validator.updatePsmDecoyScores(scoreType);
		Filter filter = new Filter(data);
		filter.setPsmScoreThreshold(new Score(ScoreType.PSM_Q_VALUE, fdr));
		filterAndGroup(filter,"PSM FDR filter");
	}
	
	private void processPeptideFdr() {
		validator.updatePsmDecoyScores(scoreType);
		validator.updatePeptideProbabilities();
		validator.updatePeptideDecoyScores(ScoreType.PEPTIDE_P_VALUE);
		Filter filter = new Filter(data);
		filter.setPeptideScoreThreshold(new Score(ScoreType.PEPTIDE_Q_VALUE, fdr));
		filterAndGroup(filter,"Peptide FDR filter");
	}
	
	private void processGroupFdr() {
		processPeptideFdr();
		
		PAnalyzer.Counts curCount = pAnalyzer.getCounts(), prevCount;
		int i = 0;
		Filter filter = new Filter(data);
		filter.setGroupScoreThreshold(new Score(ScoreType.GROUP_Q_VALUE, fdr));
		do {
			i++;
			validator.updateGroupProbabilities();
			validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
			filterAndGroup(filter,String.format("Group FDR filter, iteration %s", i));
			prevCount = curCount;
			curCount = pAnalyzer.getCounts();
		} while( !curCount.equals(prevCount) && i < MAXITER );
		if( i >= MAXITER )
			logger.warning("Maximum number of iterations reached!");
		
		validator.updateGroupProbabilities();
		validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
	}
	
	private void filterAndGroup( Filter filter, String title ) {
		filter.run();
		logCounts(title);
		rebuildGroups();
	}
	
	private void inputFilter() {
		validator.logFdrs();
		Filter filter = new Filter(data);
		filter.setOnlyBestPsmPerPrecursor(scoreType);
		filter.setMinPeptideLength(7);
		filter.setFilterDecoyPeptides(false);
		filterAndGroup(filter,"Input filter");
		validator.logFdrs();
	}
	
	private void rebuildGroups() {
		//logger.info("Updating protein groups ...");
		pAnalyzer.run();
		PAnalyzer.Counts counts = pAnalyzer.getCounts();
		logger.info("Re-grouped: "+counts.toString());
	}
	
	private void finalSteps() {
		validator.logFdrs();
		/*Filter filter = new Filter(data);
		filter.setFilterDecoyPeptides(true);
		filterAndGroup(filter,"Decoy removal");*/
		logCounts("Final counts");
		PAnalyzer.Counts counts = pAnalyzer.getCounts();
		logger.info(counts.toString());
	}
	
	private Configuration load( String path ) throws Exception {
		Configuration cfg = loadConfig(path);		
		MsMsData tmp;
		for( Configuration.InputFile input : cfg.inputs ) {
			file = new Mzid();		
			tmp = file.load(input.path,input.decoyRegex);
			if( data == null ) {
				data = tmp;
				logCounts("Loaded");
			} else {
				data.merge(tmp);
				logCounts("Merged");
			}
		}
		return cfg;
	}
	
	private Configuration loadConfig( String path ) throws JAXBException {		
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		Configuration cfg = (Configuration)um.unmarshal(new File(path));
		logger.info(String.format("Using %s: %s", path, cfg.description));
		return cfg;
	}
	
	private void save( Configuration cfg ) throws Exception {
		if( cfg.inputs.size() == 1 )
			file.save(cfg.output);
		SpHppCsv csv = new SpHppCsv(data);
		csv.save(cfg.output);
	}
	
	private void logCounts( String title ) {
		logger.info(String.format("%s: %s", title, data.toString()));
	}
	
	//private void dump() {
		// Dump Proteins
		/*List<String> monitor = Arrays.asList("?????");
		for( Protein protein : data.getProteins() )
			if( monitor.contains(protein.getAccession()) ) {
				System.out.println(protein.getAccession()+"-"+protein.getConfidence());
				for( Peptide peptide : protein.getPeptides() ) {
					System.out.print(peptide.toString()+": ");
					for( Protein protein2 : peptide.getProteins() )
						System.out.print(protein2.getAccession()+" ");
					System.out.println();
				}
				System.out.println();
			}*/

		// Dump PSMs
		/*for( Psm psm : data.getPsms() )
			System.out.println(String.format("%s:%s:%s", psm.getPeptide().getMassSequence(), psm.getMz(), psm.getScoreByType(Psm.ScoreType.MASCOT_SCORE).getValue()));*/
	//}
}