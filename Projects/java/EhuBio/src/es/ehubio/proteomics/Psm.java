package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

/**
 * Peptide-Spectrum Match in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class Psm implements Decoyable {
	public enum ScoreType {
		LARGER("Other, larger values are better", true),
		SMALLER("Other, smaller values are better", false),
		MASCOT_EVALUE("Mascot expectation value",false),
		MASCOT_SCORE("Mascot score",true),
		SEQUEST_XCORR("SEQUEST Confidence XCorr",true),
		XTANDEM_EVALUE("X!Tandem expect",false),
		PROPHET_PROBABILITY("PeptideProphet probability score",true),
		MZID_PASS_THRESHOLD("mzIdentML SpectrumIdentificationItem passThreshold attribute",true);
		
		private final String name;
		private final boolean largerBetter;
		
		private ScoreType( String name, boolean largerBetter ) {
			this.name = name;
			this.largerBetter = largerBetter;
		}
		
		public String getName() {
			return name;
		}

		public boolean isLargerBetter() {
			return largerBetter;
		}
	}
	
	public static class Score {
		private final ScoreType type;
		private final String name;
		private final double value;
		
		public Score( ScoreType type, String name, double value ) {
			this.type = type;
			this.name = name;
			this.value = value;
		}
		
		public Score( ScoreType type, double value ) {
			this.type = type;
			this.name = type.name;
			this.value = value;
		}
		
		public ScoreType getType() {
			return type;
		}
		
		public String getName() {
			return name;
		}
		
		public double getValue() {
			return value;
		}
		
		public int compare( double value2 ) {
			if( type.isLargerBetter() )
				value2 = value-value2;
			else
				value2 = value2-value;
			return (int)Math.signum(value2);
		}
	}
	
	private static int idCount = 1;
	private final int id;
	private int charge;
	private double mz;
	private Integer rank;
	private Set<Score> scores = new HashSet<>();
	private Spectrum spectrum;
	private Peptide peptide;
	
	public Psm() {
		id = idCount++;
	}
	
	public int getId() {
		return id;
	}
	
	public int getCharge() {
		return charge;
	}
	
	public void setCharge(int charge) {
		this.charge = charge;
	}
	
	public double getMz() {
		return mz;
	}
	public void setMz(double mz) {
		this.mz = mz;
	}
	
	public Integer getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public Set<Score> getScores() {
		return scores;
	}
	
	@Override
	public Score getScoreByType( ScoreType type ) {
		for( Score score : scores )
			if( score.type == type )
				return score;
		return null;
	}
	
	public Score getScoreByName( String name ) {
		for( Score score : scores )
			if( score.name == name )
				return score;
		return null;
	}
	
	public void addScore(Score score) {
		this.scores.add(score);
	}
	
	public Spectrum getSpectrum() {
		return spectrum;
	}
	
	public void linkSpectrum(Spectrum spectrum) {
		/*if( this.spectrum != null )
			this.spectrum.getPsms().remove(this);*/
		this.spectrum = spectrum;
		if( spectrum != null )
			this.spectrum.addPsm(this);
	}
	
	public Peptide getPeptide() {
		return peptide;
	}

	public void linkPeptide(Peptide peptide) {
		/*if( this.peptide != null )
			this.peptide.getPsms().remove(this);*/
		this.peptide = peptide;
		if( peptide != null )
			peptide.addPsm(this);
	}
	
	@Override
	public Boolean getDecoy() {
		if( peptide == null )
			return null;
		return peptide.getDecoy();
	}
	
	@Override
	public boolean skip() {
		return false;
	}
}