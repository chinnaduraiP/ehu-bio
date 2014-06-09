package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.proteomics.Psm.ScoreType;

/**
 * Peptide in a MS/MS proteomics experiment with PAnalyzer confidence category.
 * 
 * @author gorka
 *
 */
public class Peptide {
	public enum Confidence {
		UNIQUE, DISCRIMINATING, NON_DISCRIMINATING
	}
	
	private static int idCount = 1;
	private final int id;
	private String sequence;
	private Boolean decoy;
	private Set<Ptm> ptms = new HashSet<>();
	private Confidence confidence;
	private Set<Protein> proteins = new HashSet<>();
	private Set<Psm> psms = new HashSet<>();
	
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
	
	public Boolean getDecoy() {
		return decoy;
	}

	public void setDecoy(Boolean decoy) {
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
	
	public Set<Psm> getPsms() {
		return psms;
	}
	
	public Psm getBestPsm( ScoreType type ) {		
		Psm bestPsm = null;
		for( Psm psm : getPsms() ) {
			Double score = psm.getScoreByType(type);
			if( score == null )
				continue;
			if( bestPsm != null && score <= bestPsm.getScoreByType(type) )
				continue;
			bestPsm = psm;				
		}
		return bestPsm;
	}

	public boolean addPsm(Psm psm) {
		if( psms.add(psm) ) {
			if( psm.getPeptide() != this )
				psm.linkPeptide(this);
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getSequence());
		if( getConfidence() == null )
			builder.append('?');
		else if( getConfidence() == Confidence.DISCRIMINATING )
			builder.append('*');
		else if( getConfidence() == Confidence.NON_DISCRIMINATING )
			builder.append("**");
		return builder.toString();
	}	
}
