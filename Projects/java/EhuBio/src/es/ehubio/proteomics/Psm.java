package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

public class Psm {
	public enum ScoreType {
		OTHER("Other"),		
		MASCOT_EVALUE("Mascot expectation value"),
		SEQUEST_XCORR("SEQUEST Confidence XCorr"),
		XTANDEM_EVALUE("X!Tandem expect");
		
		public final String name;
		
		private ScoreType( String name ) {
			this.name = name;
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
	
	public Double getScoreByType( ScoreType type ) {
		for( Score score : scores )
			if( score.type == type )
				return score.value;
		return null;
	}
	
	public Double getScoreByName( String name ) {
		for( Score score : scores )
			if( score.name == name )
				return score.value;
		return null;
	}
	
	public void setScores(Set<Score> scores) {
		this.scores = scores;
	}
	
	public Spectrum getSpectrum() {
		return spectrum;
	}
	
	public void linkSpectrum(Spectrum spectrum) {
		if( this.spectrum != null )
			this.spectrum.removePsm(this);
		this.spectrum = spectrum;
		if( spectrum != null )
			spectrum.addPsm(this);
	}
	
	public Peptide getPeptide() {
		return peptide;
	}

	public void linkPeptide(Peptide peptide) {
		this.peptide = peptide;
		peptide.addPsm(this);
	}
}