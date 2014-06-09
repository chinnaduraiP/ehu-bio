package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

/**
 * Ambiguity group of proteins in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class ProteinGroup {
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

	public int getId() {
		return id;
	}
}