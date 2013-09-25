package es.ehu.grk.wregex;

import es.ehu.grk.db.Fasta;

public final class TrainingMotif {
	public final int start;
	public final int end;
	public final double weight;
	public final String motif;
	private final Fasta fasta;
	private int combinations;

	public TrainingMotif(Fasta fasta, int start, int end, double weight) {
		this.fasta = fasta;
		this.start = start;
		this.end = end;
		this.weight = weight;
		this.motif = fasta.sequence().substring(start, end+1);
		this.combinations = 1;
	}
	
	public String getSequence() {
		return fasta.sequence();
	}
	
	public String getAccession() {
		return fasta.guessAccession();
	}

	public int getCombinations() {
		return combinations;
	}

	public void setCombinations(int combinations) {
		this.combinations = combinations;
	}
	
	public double getDividedWeight() {
		return weight/combinations;
	}
}
