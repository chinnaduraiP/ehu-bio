package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.Util;
import es.ehubio.proteomics.Protein.Confidence;

/**
 * Ambiguity group of proteins in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class ProteinGroup extends DecoyBase {
	private static int idCount = 1;
	private final int id;
	private Set<Protein> proteins = new HashSet<>();
	
	public ProteinGroup() {
		id = idCount++;
	}

	public Set<Protein> getProteins() {
		return proteins;
	}
	
	public Protein firstProtein() {
		if( proteins.isEmpty() )
			return null;
		return proteins.iterator().next();
	}

	public boolean addProtein( Protein protein ) {
		if( proteins.add(protein) ) {
			protein.setGroup(this);
			return true;
		}
		return false;
	}
	
	public int size() {
		return proteins.size();
	}
	
	public Protein.Confidence getConfidence() {
		return firstProtein().getConfidence();
	}
	
	@Override
	public Boolean getDecoy() {
		if( getProteins().isEmpty() )
			return null;
		
		boolean nullDecoy = false;
		for( Protein protein : getProteins() )
			if( Boolean.FALSE.equals(protein.getDecoy()) )
				return false;
			else if( protein.getDecoy() == null )
				nullDecoy = true;
		return nullDecoy ? null : true;
	}
	
	
	@Override
	public void setDecoy(Boolean decoy) {
		for( Protein protein : getProteins() )
			protein.setDecoy(decoy);
	}

	public int getId() {
		return id;
	}
	
	public Protein getBestProtein( ScoreType type ) {
		return getBest(getProteins(), type);
	}

	@Override
	public Score getScoreByType(ScoreType type) {
		Score score = super.getScoreByType(type);
		if( score != null )
			return score;
		
		DecoyBase best = getBestOwnPsm(type);
		if( best != null )
			return best.getScoreByType(type);
		
		best = getBestOwnPeptide(type);
		if( best != null )
			return best.getScoreByType(type);
		
		best = getBestProtein(type);
		if( best != null )
			return best.getScoreByType(type);
		
		return null;
	}
	
	@Override
	public boolean skipFdr() {
		return getConfidence() == Confidence.NON_CONCLUSIVE;
	}
	
	public String buildName() {
		Set<String> names = new HashSet<>();
		for( Protein protein : getProteins() )
			names.add(protein.getAccession());
		return Util.mergeStrings(names);
	}
	
	@Override
	protected String buildUniqueString() {
		return ""+id;
	}
	
	public Set<Peptide> getPeptides() {
		Set<Peptide> peptides = new HashSet<>();
		for( Protein protein : getProteins() )
			peptides.addAll(protein.getPeptides());
		return peptides;
	}
	
	public Set<Psm> getPsms() {
		Set<Psm> psms = new HashSet<>();
		for( Peptide peptide : getPeptides() )
			psms.addAll(peptide.getPsms());
		return psms;
	}
	
	public Peptide getBestPeptide( ScoreType type ) {
		return getBest(getPeptides(), type);
	}
	
	public Psm getBestPsm( ScoreType type ) {
		return getBest(getPsms(), type);
	}
	
	public Set<Peptide> getOwnPeptides() {
		Set<Peptide> peptides = new HashSet<>();
		for( Protein protein : getProteins() )
			for( Peptide peptide : protein.getPeptides() )
				if( peptide.getConfidence() != Peptide.Confidence.NON_DISCRIMINATING )
					peptides.add(peptide);
		return peptides;
	}
	
	public Set<Psm> getOwnPsms() {
		Set<Psm> psms = new HashSet<>();
		for( Peptide peptide : getOwnPeptides() )
			psms.addAll(peptide.getPsms());
		return psms;
	}
	
	public Peptide getBestOwnPeptide( ScoreType type ) {
		return getBest(getOwnPeptides(), type);
	}
	
	public Psm getBestOwnPsm( ScoreType type ) {
		return getBest(getOwnPsms(), type);
	}
}