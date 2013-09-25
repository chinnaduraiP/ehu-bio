package es.ehu.grk.wregex;

import java.io.Serializable;

import es.ehu.grk.db.Fasta;

public final class InputMotif implements Serializable {
	private static final long serialVersionUID = 1L;
	private int start;	
	private int end;
	private double weight;
	public final Fasta fasta;

	public InputMotif(Fasta fasta, int start, int end, double weight) {
		this.fasta = fasta;
		this.start = start;
		this.end = end;
		this.weight = weight;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public String getMotif() {
		return fasta.sequence().substring(start-1, end);
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public String getAccession() {
		return fasta.guessAccession();
	}
	
	public boolean contains(Result result) {
		if( result.start >= start && result.start <= end )
			return true;
		if( result.end >= start && result.end <= end )
			return true;
		if( start >= result.start && start <= result.end )
			return true;
		if( end >= result.start && end <= result.end )
			return true;
		return false;
	}
}
