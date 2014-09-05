package es.ehubio.tools;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.io.Mzid;
import es.ehubio.proteomics.io.EhubioCsv;
import es.ehubio.proteomics.pipeline.Filter;
import es.ehubio.proteomics.pipeline.PAnalyzer;
import es.ehubio.proteomics.pipeline.Validator;

public final class PAnalyzerCli implements Command.Interface {
	private static final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());
	private MsMsData data;
	private MsMsFile file;
	private PAnalyzer pAnalyzer;
	private Validator validator;
	private ScoreType psmScoreType;
	private final static int MAXITER=15;
	private Configuration cfg;	
	private boolean loadIons = false;
	private boolean saveResults = true;
	
	public boolean isLoadIons() {
		return loadIons;
	}

	public void setLoadIons(boolean loadIons) {
		this.loadIons = loadIons;
	}
	
	public boolean isSaveResults() {
		return saveResults;
	}

	public void setSaveResults(boolean saveResults) {
		this.saveResults = saveResults;
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
		load(args[0]);		
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
		if( isSaveResults() )
			save();
		
		logger.info("finished!");
	}
	
	public Configuration getConfiguration() {
		return cfg;
	}
	
	public MsMsData getData() {
		return data;
	}
	
	private void initialize() {
		pAnalyzer = new PAnalyzer(data);		
		validator = new Validator(data);
	}

	private void processPsmFdr() {				
		validator.updatePsmDecoyScores(psmScoreType);
		Filter filter = new Filter(data);
		filter.setPsmScoreThreshold(new Score(ScoreType.PSM_Q_VALUE, cfg.psmFdr));
		filterAndGroup(filter,String.format("PSM FDR=%s filter",cfg.psmFdr));
	}
	
	private void processPeptideFdr() {
		validator.updatePsmDecoyScores(psmScoreType);
		validator.updatePeptideProbabilities();
		validator.updatePeptideDecoyScores(ScoreType.PEPTIDE_P_VALUE);
		Filter filter = new Filter(data);
		filter.setPeptideScoreThreshold(new Score(ScoreType.PEPTIDE_Q_VALUE, cfg.peptideFdr));
		filterAndGroup(filter,String.format("Peptide FDR=%s filter",cfg.peptideFdr));
	}
	
	private void processGroupFdr() {
		processPeptideFdr();
		
		PAnalyzer.Counts curCount = pAnalyzer.getCounts(), prevCount;
		int i = 0;
		Filter filter = new Filter(data);
		filter.setGroupScoreThreshold(new Score(ScoreType.GROUP_Q_VALUE, cfg.groupFdr));
		do {
			i++;
			validator.updateGroupProbabilities();
			validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
			filterAndGroup(filter,String.format("Group FDR=%s filter, iteration %s",cfg.groupFdr,i));
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
		filter.setOnlyBestPsmPerPrecursor(psmScoreType);
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
	
	private void load( String path ) throws Exception {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		cfg = (Configuration)um.unmarshal(new File(path));
		psmScoreType = ScoreType.getByName(cfg.psmScore);
		logger.info(String.format("Using %s: %s", path, cfg.description));	
		
		MsMsData tmp;
		for( Configuration.InputFile input : cfg.inputs ) {
			file = new Mzid();		
			tmp = file.load(input.path,input.decoyRegex);
			if( isLoadIons() )
				file.loadIons(input.ions);
			if( data == null ) {
				data = tmp;
				logCounts("Loaded");
			} else {
				data.merge(tmp);
				logCounts("Merged");
			}
		}
	}

	private void save() throws Exception {
		if( cfg.inputs.size() == 1 )
			file.save(cfg.output);
		EhubioCsv csv = new EhubioCsv(data);
		csv.setPsmScoreType(psmScoreType);
		csv.save(cfg.output);
	}
	
	private void logCounts( String title ) {
		logger.info(String.format("%s: %s", title, data.toString()));
	}

	@XmlRootElement
	public static class Configuration {
		public static class InputFile {
			public String path;			
			public String decoyRegex;
			public String ions;
		}
		public String description;
		public String operation;
		public String psmScore;
		public Double psmFdr;
		public Double peptideFdr;
		public Double groupFdr;
		@XmlElement(name="input")
		public List<InputFile> inputs;
		public String output;
	}
}