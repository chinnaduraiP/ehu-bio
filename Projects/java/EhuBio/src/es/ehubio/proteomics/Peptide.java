package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

public class Peptide {
	public enum Confidence {
		UNIQUE, DISCRIMINATING, NON_DISCRIMINATING
	}
	
	private static int idCount = 1;
	private final int id;
	private String sequence;
	private boolean decoy = false;
	private Set<Ptm> ptms = new HashSet<>();
	private Confidence confidence;
	private Set<Protein> proteins = new HashSet<>();	
	
	public Peptide() {
		id = idCount++;
	}

	public int getId() {
		return id;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public boolean isDecoy() {
		return decoy;
	}

	public void setDecoy(boolean decoy) {
		this.decoy = decoy;
	}
	
	public boolean addPtm( Ptm ptm ) {
		return ptms.add(ptm);
	}

	public Set<Ptm> getPtms() {
		return ptms;
	}

	public Confidence getConfidence() {
		return confidence;
	}

	public void setConfidence(Confidence confidence) {
		this.confidence = confidence;
	}

	public Set<Protein> getProteins() {
		return proteins;
	}

	public boolean addProtein(Protein protein) {
		if( proteins.add(protein) ) {
			protein.addPeptide(this);
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getSequence());
		if( getConfidence() == Confidence.DISCRIMINATING )
			builder.append('*');
		else if( getConfidence() == Confidence.NON_DISCRIMINATING )
			builder.append("**");
		return builder.toString();
	}
}
