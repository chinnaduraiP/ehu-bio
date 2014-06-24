package es.ehubio.tools;

import java.util.logging.Logger;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.io.Mzid;
import es.ehubio.proteomics.pipeline.Filter;
import es.ehubio.proteomics.pipeline.PAnalyzer;
import es.ehubio.proteomics.pipeline.Validator;

public final class PAnalyzerCli implements Command.Interface {
	private static final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());
	private MsMsData data;
	private MsMsFile file;
	private final ScoreType scoreType = ScoreType.XTANDEM_EVALUE;
	//private final ScoreType scoreType = ScoreType.XTANDEM_HYPERSCORE;
	private final double fdr = 0.01;

	@Override
	public String getUsage() {
		return "</path/input.mzid> </path/output.mzid>";
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}	

	@Override
	public void run(String[] args) throws Exception {
		load(args[0],"decoy");
		filter();		
		//dump();
		validate();
		//save(args[1]);
		logger.info("finished!");
	}	
	
	private void filter() {
		// PAnalyzer
		logger.info("Running PAnalyzer ...");
		PAnalyzer pAnalyzer = new PAnalyzer(data);
		pAnalyzer.run();
		PAnalyzer.Counts counts = pAnalyzer.getCounts();
		logger.info(counts.toString());

		// Filter		
		logger.info("Filtering data ...");
		Filter filter = new Filter(data);
		//filter.setRankTreshold(1);
		filter.setPpmThreshold(10.0);
		//Score score = new Score(ScoreType.XTANDEM_EVALUE,0.33);
		//Score score = new Score(ScoreType.XTANDEM_EVALUE,29);
		//Score score = new Score(ScoreType.XTANDEM_HYPERSCORE,20.3);
		//Score score = new Score(ScoreType.XTANDEM_HYPERSCORE,9.5);
		//filter.setPsmScoreThreshold(score);
		filter.setMinPeptideLength(7);
		//filter.setFilterDecoyPeptides(true);
		//filter.setMzidPassThreshold(true);
		//Score score = new Score(ScoreType.XTANDEM_EVALUE,0.1);
		//filter.setPeptideScoreThreshold(score);
		//Score score = new Score(ScoreType.XTANDEM_EVALUE,0.041);
		//filter.setProteinScoreThreshold(score);
		//Score score = new Score(ScoreType.XTANDEM_EVALUE,0.046);
		//filter.setGroupScoreThreshold(score);
		filter.run();
		//filter.runGroupFdrThreshold(scoreType, fdr);
		logger.info(String.format("Filter: %d groups, %d proteins, %d peptides, %d psms, %d spectra", data.getGroups().size(), data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));

		// PAnalyzer
		logger.info("Running PAnalyzer again ...");
		pAnalyzer.run();
		counts = pAnalyzer.getCounts();
		logger.info(counts.toString());
	}	
	
	private void validate() {
		logger.info("Running Validator ...");
		Validator validator = new Validator(data);
		//validator.setCountDecoy(true);
		logger.info(String.format("FDR -> PSM: %s, Peptide: %s, Protein: %s, Group: %s",
			validator.getPsmFdr().getRatio(), validator.getPeptideFdr().getRatio(), validator.getProteinFdr().getRatio(), validator.getGroupFdr().getRatio()));
		logger.info(String.format("Thresholds for FDR=%s -> PSM: %s, Peptide: %s, Protein: %s, Group: %s",
			fdr,validator.getPsmFdrThreshold(scoreType, fdr),validator.getPeptideFdrThreshold(scoreType, fdr),validator.getProteinFdrThreshold(scoreType, fdr),validator.getGroupFdrThreshold(scoreType, fdr)));
	}
	
	private void load( String path, String decoy ) throws Exception {
		file = new Mzid();		
		data = file.load(path,decoy);
		logger.info(String.format("Loaded: %d groups, %d proteins, %d peptides, %d psms, %d spectra", data.getGroups().size(), data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));
	}
	
	/*private void save( String path ) throws Exception {
		file.save(path);
	}*/
	
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