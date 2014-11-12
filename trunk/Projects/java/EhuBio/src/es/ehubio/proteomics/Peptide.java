package es.ehubio.proteomics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Peptide in a MS/MS proteomics experiment with PAnalyzer confidence category.
 * 
 * @author gorka
 *
 */
public class Peptide extends DecoyBase {
	public enum Confidence {
		UNIQUE(0), DISCRIMINATING(1), NON_DISCRIMINATING(2);
		
		private Confidence( int order ) {
			this.order = order;
		}
		public int getOrder() {
			return order;
		}
		private final int order;
	}
	
	private static int idCount = 1;
	private final int id;
	private String sequence;
	private Set<Ptm> ptms = new HashSet<>();
	private Confidence confidence;
	private Set<Protein> proteins = new HashSet<>();
	private Set<Psm> psms = new HashSet<>();
	private String name;
	
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
		return getBest(getPsms(), type);
	}
	
	@Override
	public Score getScoreByType(ScoreType type) {
		Score score = super.getScoreByType(type);
		if( score != null )
			return score;
		
		Psm bestPsm = getBestPsm(type);
		if( bestPsm != null )
			return bestPsm.getScoreByType(type);
		
		return null;
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
		//StringBuilder builder = new StringBuilder(getSequence());
		StringBuilder builder = new StringBuilder(""+getId());
		if( getConfidence() == null )
			builder.append('?');
		else if( getConfidence() == Confidence.DISCRIMINATING )
			builder.append('*');
		else if( getConfidence() == Confidence.NON_DISCRIMINATING )
			builder.append("**");
		return builder.toString();
	}
	
	public String getMassSequence() {
		StringBuilder str = new StringBuilder();
		
		for( int i = 0; i < sequence.length(); i++ ) {
			List<Double> localized = new ArrayList<>();
			for( Ptm ptm : getPtms() ) {
				if( ptm.getPosition() == null || ptm.getPosition()-1 != i )
					continue;
				localized.add(ptm.getMassDelta());
			}
			addMassString(str, sequence.charAt(i), localized);
		}
		
		List<Double> unlocalized = new ArrayList<>();
		for( Ptm ptm : getPtms() )
			if( ptm.getPosition() == null )
				unlocalized.add(ptm.getMassDelta());
		addMassString(str, '?', unlocalized);
		
		return str.toString();
	}
	
	private void addMassString(StringBuilder str, char aa, List<Double> list) {
		if( aa == '?' && list.isEmpty() )
			return;
		str.append(aa);
		Collections.sort(list);
		for( Double mass : list ) {			
			if( mass == null )
				str.append("(?)");
			else if( mass < 0 )
				str.append(String.format("(%.2f)", mass));
			else
				str.append(String.format("(+%.2f)", mass));
		}			
	}
	
	@Override
	protected String buildUniqueString() {
		return getMassSequence();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getReplicates() {
		Set<String> set = new HashSet<>();
		for( Psm psm : getPsms() )
			set.add(psm.getSpectrum().getRepName());
		return set;
	}
}