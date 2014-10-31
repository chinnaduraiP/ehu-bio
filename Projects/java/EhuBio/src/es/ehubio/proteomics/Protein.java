package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.db.fasta.Fasta;

/**
 * Protein in a MS/MS proteomics experiment with PAnalyzer confidence category.
 * 
 * @author gorka
 *
 */
public class Protein extends DecoyBase {
	public enum Confidence {
		CONCLUSIVE, INDISTINGUISABLE_GROUP, AMBIGUOUS_GROUP, NON_CONCLUSIVE 
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
	
	public void setFasta( Fasta fasta ) {
		setAccession(fasta.getAccession());
		setDescription(fasta.getDescription());
		setName(fasta.getProteinName());
		setSequence(fasta.getSequence());
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
	
	@Override
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
	
	@Override
	public void setDecoy(Boolean decoy) {
		for( Peptide peptide : getPeptides() )
			peptide.setDecoy(decoy);
	}
	
	public Peptide getBestPeptide( ScoreType type ) {		
		Peptide best = null;
		for( Peptide peptide : getPeptides() ) {
			Score score = peptide.getScoreByType(type);
			if( score == null )
				continue;
			if( best != null && best.getScoreByType(type).compare(score.getValue()) >= 0 )
				continue;
			best = peptide;				
		}
		return best;
	}

	@Override
	public Score getScoreByType(ScoreType type) {
		Score score = super.getScoreByType(type);
		if( score != null )
			return score;
		
		Peptide best = getBestPeptide(type);
		if( best == null )
			return null;
		return best.getScoreByType(type);
	}
	
	@Override
	public String toString() {
		return getAccession();
	}
	
	@Override
	protected String buildUniqueString() {
		return getAccession();
	}
	
	public Set<String> getReplicates() {
		Set<String> set = new HashSet<>();
		for( Peptide peptide : getPeptides() )
			set.addAll(peptide.getReplicates());
		return set;
	}
}