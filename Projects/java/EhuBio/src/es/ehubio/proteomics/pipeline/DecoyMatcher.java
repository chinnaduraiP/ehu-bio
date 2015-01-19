package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.ehubio.proteomics.Protein;

public class DecoyMatcher implements RandomMatcher {
	public DecoyMatcher( Collection<Protein> proteins, String decoyPrefix ) {
		this.decoyPrefix = decoyPrefix;
		for( Protein protein : proteins )
			if( Boolean.TRUE.equals(protein.getDecoy()) )
				mapDecoys.put(protein.getAccession(), protein);
	}
	
	public boolean checkPrefix( Collection<Protein> proteins ) {
		for( Protein protein : proteins )
			if( !Boolean.TRUE.equals(protein.getDecoy()) )
				if( mapDecoys.get(decoyPrefix+protein.getAccession()) != null )
					return true;
		return false;
	}

	@Override
	public double getNq(Protein protein) {
		if( Boolean.TRUE.equals(protein.getDecoy()) )
			return protein.getPeptides().size();
		Protein decoy = mapDecoys.get(decoyPrefix+protein.getAccession());
		if( decoy == null )
			return 1;
		return decoy.getPeptides().size();
	}
	
	private final Map<String, Protein> mapDecoys = new HashMap<>();
	private final String decoyPrefix;
}
