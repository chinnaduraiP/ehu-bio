package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class DecoyMatcher implements RandomMatcher {
	public DecoyMatcher( Collection<Protein> proteins, String decoyTag, boolean shared ) {
		for( Protein protein : proteins )
			if( Boolean.TRUE.equals(protein.getDecoy()) )
				mapDecoys.put(protein.getAccession().replaceAll(decoyTag,""), protein);
		this.shared = shared;
	}
	
	public boolean checkPrefix( Collection<Protein> proteins ) {
		for( Protein protein : proteins )
			if( !Boolean.TRUE.equals(protein.getDecoy()) )
				if( mapDecoys.get(protein.getAccession()) != null )
					return true;
		return false;
	}

	public double getNq(Protein protein) {
		if( Boolean.TRUE.equals(protein.getDecoy()) )
			return protein.getPeptides().size();
		Protein decoy = mapDecoys.get(protein.getAccession());
		if( decoy == null )
			return 1;
		return decoy.getPeptides().size();
	}	

	public double getMq(Protein protein) {
		Protein decoy = Boolean.TRUE.equals(protein.getDecoy()) ? protein : mapDecoys.get(protein.getAccession());
		if( decoy == null )
			return 1;
		double Mq = 0.0;
		for( Peptide peptide : decoy.getPeptides() )
			Mq += 1.0/peptide.getProteins().size();
		return Mq;
	}
	
	@Override
	public double getExpected(Protein protein) {
		return shared ? getMq(protein) : getNq(protein);
	}
	
	private final Map<String, Protein> mapDecoys = new HashMap<>();
	private final boolean shared;
}
