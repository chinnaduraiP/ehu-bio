package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.FragmentIon;
import es.ehubio.proteomics.Masses;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Ptm;

public class Fragmenter {
	private static final Map<Character, Double> mapMasses = new HashMap<>();
	private List<FragmentIon> totalIons = new ArrayList<>();
	private final Psm psm;
	private final double[] partialMasses;
	
	static {
		for(Masses mass : Masses.values() )
			mapMasses.put(mass.getLetter(), mass.getMass());
	}
	
	public Fragmenter( Psm psm ) {
		this.psm = psm;
		partialMasses = getPartialMasses(psm.getPeptide());
	}

	private static double[] getPartialMasses( Peptide peptide ) {
		double[] masses = new double[peptide.getSequence().length()];
		double mass;
		for( int i = 0; i < masses.length; i++ ) {
			mass = 0.0;
			for( Ptm ptm : peptide.getPtms() )
				if( ptm.getPosition()-1 == i )
					mass += ptm.getMassDelta();
			mass += mapMasses.get(Character.toUpperCase(peptide.getSequence().charAt(i)));
			masses[i]=mass;
		}
		return masses;
	}
	
	public void addBIons() {
		
	}
}
