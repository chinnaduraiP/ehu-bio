package es.ehubio.tools;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.io.MayuCsv;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.pipeline.Validator;
import es.ehubio.proteomics.pipeline.Filter;
import es.ehubio.proteomics.pipeline.PAnalyzer;

public final class PAnalyzerCli implements Command.Interface {
	private static final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());

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
		MsMsFile file = new MayuCsv();//Mzid();
		MsMsData data = file.load(args[0],"rev_");//"decoy");
		logger.info(String.format("Loaded: %d groups, %d proteins, %d peptides, %d psms, %d spectra", data.getGroups().size(), data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));
		
		// Filter		
		logger.info("Filtering data ...");
		Filter filter = new Filter();
		//filter.setPsmScoreThreshold(new Psm.Score(Psm.ScoreType.MASCOT_EVALUE, 0.05));
		//filter.setPsmScoreThreshold(new Psm.Score(Psm.ScoreType.PROPHET_PROBABILITY, 0.7142));
		filter.setPsmScoreThreshold(new Psm.Score(Psm.ScoreType.PROPHET_PROBABILITY, 0.96));
		//filter.setMinPeptideLength(7);
		filter.setFilterDecoyPeptides(false);
		filter.setMzidPassThreshold(false);
		filter.run(data);
		logger.info(String.format("Filter: %d groups, %d proteins, %d peptides, %d psms, %d spectra", data.getGroups().size(), data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));
		
		// PAnalyzer
		logger.info("Running PAnalyzer ...");
		PAnalyzer pAnalyzer = new PAnalyzer();
		pAnalyzer.run(data);
		logger.info("done!");
		
		// Stats
		int conclusive = 0;
		int nonconclusive = 0;
		int indistinguishable = 0;
		int ambigous = 0;
		for( ProteinGroup group : data.getGroups() ) {			
			switch (group.getConfidence()) {
				case CONCLUSIVE:
					//System.out.println(group.firstProtein().getAccession());
					conclusive++;
					break;
				case NON_CONCLUSIVE:
					//System.out.println(group.firstProtein().getAccession());
					nonconclusive++;
					break;
				case INDISTINGUISABLE_GROUP:
					/*for( Protein protein : group.getProteins() )
						System.out.println(protein.getAccession());*/
					indistinguishable++;
					break;
				case AMBIGUOUS_GROUP:
					ambigous++;
					break;
			}
		}
		List<String> monitor = Arrays.asList("?????");
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
			}
		logger.info(String.format("Groups: %d, Minimum: %d", data.getGroups().size(), conclusive+indistinguishable+ambigous));
		logger.info(String.format("Conclusive: %d, Non-Conclusive: %d, Indistiguishable: %d, Ambigous: %d",conclusive,nonconclusive,indistinguishable,ambigous));
		
		logger.info("Running Extractor ...");
		Validator validator = new Validator();
		validator.setData(data);
		//validator.setCountDecoy(true);
		logger.info(String.format("PSM FDR: %s", validator.getPsmFdr().getRatio()));
		logger.info(String.format("Peptide FDR: %s", validator.getPeptideFdr().getRatio()));
		logger.info(String.format("Protein FDR: %s", validator.getProteinFdr().getRatio()));
		logger.info(String.format("Group FDR: %s", validator.getGroupFdr().getRatio()));
		double fdr = 0.01;
		logger.info(String.format("PSM threshold for FDR=%s -> %s",fdr,validator.getProteinFdrThreshold(Psm.ScoreType.PROPHET_PROBABILITY, fdr)));
		
		//file.save(args[1]);
		logger.info("finished!!");
	}
}