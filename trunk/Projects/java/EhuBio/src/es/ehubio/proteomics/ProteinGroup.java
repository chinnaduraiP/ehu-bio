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
		Protein best = null;
		for( Protein protein : getProteins() ) {
			Score score = protein.getScoreByType(type);
			if( score == null )
				continue;
			if( best != null && best.getScoreByType(type).compare(score.getValue()) >= 0 )
				continue;
			best = protein;				
		}
		return best;
	}

	@Override
	public Score getScoreByType(ScoreType type) {
		Score score = super.getScoreByType(type);
		if( score != null )
			return score;
		
		Protein best = getBestProtein(type);
		if( best == null )
			return null;
		return best.getScoreByType(type);
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
	public String getUniqueString() {
		return ""+id;
	}
}