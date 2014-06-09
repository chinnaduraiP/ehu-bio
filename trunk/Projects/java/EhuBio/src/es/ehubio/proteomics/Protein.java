package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

/**
 * Protein in a MS/MS proteomics experiment with PAnalyzer confidence category.
 * 
 * @author gorka
 *
 */
public class Protein {
	public enum Confidence {
		CONCLUSIVE, NON_CONCLUSIVE, AMBIGUOUS_GROUP, INDISTINGUISABLE_GROUP 
	}
	
	private static int idCount = 1;
	private final int id;
	private Confidence confidence;
	private Set<Peptide> peptides = new HashSet<>();
	private ProteinGroup group;
	private String accession;
	private String sequence;
	private String name;
	private String description;	

	public Protein() {
		id = idCount++;
	}

	public int getId() {
		return id;
	}
	
	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Confidence getConfidence() {
		return confidence;
	}

	public void setConfidence(Confidence confidence) {
		this.confidence = confidence;
	}

	public Set<Peptide> getPeptides() {
		return peptides;
	}

	public boolean addPeptide( Peptide peptide ) {
		if( peptides.add(peptide) ) {
			peptide.addProtein(this);
			return true;
		}
		return false;
	}

	public ProteinGroup getGroup() {
		return group;
	}

	public void setGroup(ProteinGroup group) {
		if( this.group == group )
			return;
		this.group = group;
		if( group != null )
			group.addProtein(this);
	}
	
	public Boolean getDecoy() {
		if( getPeptides().isEmpty() )
			return null;
		
		boolean nullDecoy = false;
		for( Peptide peptide : getPeptides() )
			if( Boolean.FALSE.equals(peptide.getDecoy()) )
				return false;
			else if( peptide.getDecoy() == null )
				nullDecoy = true;
		return nullDecoy ? null : true;
	}
}