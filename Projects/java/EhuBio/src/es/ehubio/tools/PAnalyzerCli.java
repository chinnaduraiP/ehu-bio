package es.ehubio.tools;

import java.util.logging.Logger;

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

	@Override
	public String getUsage() {
		return "<psm|grp|pepg|x> </path/input.mzid> </path/output.mzid>";
	}

	@Override
	public int getMinArgs() {
		return 3;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}	

	@Override
	public void run(String[] args) throws Exception {
		load(args[1],"decoy");		
		initialize();
		rebuildGroups();		
		inputFilter();
		switch( args[0] ) {
			case "psm":
				logger.info("--- PSM FDR based process ---");
				processPsmFdr();
				break;
			case "grp":
				logger.info("--- Protein group FDR based process (iterative) ---");
				processGroupFdr();
				break;
			case "pepg":
				logger.info("--- Peptide+Protein Group FDR based process (non-iterative) ---");
				processPeptideGroupFdr();
				break;
			default:
				logger.info("--- Non FDR based process ---");
		}
		finalSteps();		
		save(args[2]);
		
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
		filterAndGroup(filter,"FDR filter");
	}
	
	private void processGroupFdr() {
		PAnalyzer.Counts curCount = pAnalyzer.getCounts(), prevCount;
		int i = 0;
		validator.updatePsmDecoyScores(scoreType);
		Filter filter = new Filter(data);
		filter.setGroupScoreThreshold(new Score(ScoreType.GROUP_Q_VALUE, fdr));
		do {
			i++;
			validator.updatePeptideProbabilities();
			validator.updateGroupProbabilities();
			validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
			filterAndGroup(filter,String.format("Iteration %s", i));
			prevCount = curCount;
			curCount = pAnalyzer.getCounts();			
			//logger.info(String.format("Iteration: %s -> prev=%s, new=%s", i, prevCount.toString(), curCount.toString()));
		} while( !curCount.equals(prevCount) && i < MAXITER );
		if( i >= MAXITER )
			logger.warning("Maximum number of iterations reached!");
		
		validator.updateGroupProbabilities();
		validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
	}
	
	private void processPeptideGroupFdr() {
		validator.updatePsmDecoyScores(scoreType);
		validator.updatePeptideProbabilities();
		validator.updatePeptideDecoyScores(ScoreType.PEPTIDE_P_VALUE);
		Filter filter = new Filter(data);
		filter.setPeptideScoreThreshold(new Score(ScoreType.PEPTIDE_Q_VALUE, fdr));
		filterAndGroup(filter, "Peptide FDR filter");
		
		validator.updateGroupProbabilities();
		validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
		filter = new Filter(data);
		filter.setGroupScoreThreshold(new Score(ScoreType.GROUP_Q_VALUE, fdr));
		filterAndGroup(filter,"Group FDR filter");
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
	
	private void load( String path, String decoy ) throws Exception {
		file = new Mzid();		
		data = file.load(path,decoy);
		logCounts("Loaded");
	}
	
	private void save( String path ) throws Exception {
		file.save(path);
		SpHppCsv csv = new SpHppCsv(data);
		csv.save(path);
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