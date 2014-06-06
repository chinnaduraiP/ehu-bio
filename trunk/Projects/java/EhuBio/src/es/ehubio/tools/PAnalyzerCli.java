package es.ehubio.tools;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import es.ehubio.proteomics.Extractor;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Mzid;
import es.ehubio.proteomics.PAnalyzer;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;

public final class PAnalyzerCli implements Command.Interface {
	private final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());

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
		Mzid mzid = new Mzid();
		MsMsData data = mzid.load(args[0]);
		data.clearMetaData();
		logger.info(String.format("Loaded: %d proteins, %d peptides, %d psms, %d spectra", data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));
		
		// Filter		
		logger.info("Filtering data ...");
		Extractor extractor = new Extractor();
		extractor.setData(data);
		Extractor.Filter filter = new Extractor.Filter();
		filter.setPsmScore(Psm.ScoreType.MASCOT_EVALUE, 0.05, false);
		filter.setMinPeptideLength(7);
		extractor.filterData(filter);
		logger.info(String.format("Filter: %d proteins, %d peptides, %d psms, %d spectra", data.getProteins().size(), data.getPeptides().size(), data.getPsms().size(), data.getSpectra().size() ));
		
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
		for( ProteinGroup group : pAnalyzer.getGroups() ) {			
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
		for( Protein protein : pAnalyzer.getProteins() )
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
		logger.info(String.format("Groups: %d", pAnalyzer.getGroups().size()));
		logger.info(String.format("Conclusive: %d, Non-Conclusive: %d, Indistiguishable: %d, Ambigous: %d",conclusive,nonconclusive,indistinguishable,ambigous));
		
		mzid.save(args[1]);
		logger.info("finished!!");
	}

}