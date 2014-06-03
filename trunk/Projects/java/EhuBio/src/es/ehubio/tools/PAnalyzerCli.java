package es.ehubio.tools;

import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.proteomics.Mzid;
import es.ehubio.proteomics.PAnalyzer;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Spectrum;

public final class PAnalyzerCli implements Command.Interface {
	private final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());

	@Override
	public String getUsage() {
		return "</path/file.mzid>";
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
		Mzid mzid = new Mzid();
		Set<Spectrum> data = mzid.load(args[0]);
		
		// PAnalyzer
		logger.info("Running PAnalyzer ...");
		PAnalyzer pAnalyzer = new PAnalyzer();
		pAnalyzer.setSpectra(data);
		pAnalyzer.buildGroups();
		logger.info("done!");
		
		// Stats
		int conclusive = 0;
		int nonconclusive = 0;
		int indistinguishable = 0;
		int ambigous = 0;
		for( ProteinGroup group : pAnalyzer.getGroups() ) {			
			switch (group.getConfidence()) {
				case CONCLUSIVE:					
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
		/*List<String> monitor = Arrays.asList("Q9BYX7","P0CG39","P0CG38");
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
			}*/
		System.out.println(String.format("Groups: %d", pAnalyzer.getGroups().size()));
		System.out.println(String.format("Conclusive: %d, Non-Conclusive: %d, Indistiguishable: %d, Ambigous: %d",conclusive,nonconclusive,indistinguishable,ambigous));		
	}

}