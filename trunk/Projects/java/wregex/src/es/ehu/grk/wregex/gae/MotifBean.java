package es.ehu.grk.wregex.gae;

import java.io.Serializable;

public class MotifBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sequence = null;
	private String header = null;
	private int start;
	private int end;
	private double weight;
	
	public MotifBean() {		
	}
	
	public MotifBean( String header, String sequence ) {
		this(header, sequence, 1, sequence.length());
	}
	
	public MotifBean( String header, String sequence, int start, int end ) {
		this(header, sequence, start, end, 100.0);
	}
	
	public MotifBean( String header, String sequence, int start, int end, double weight ) {
		this.header = header;
		this.sequence = sequence;
		this.start = start;
		this.end = end;
		this.weight = weight;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public String getMotif() {
		return sequence.substring(start-1, end);
	}
	
	public String getHeader() {
		return header;
	}
	
	public void setHeader(String header) {
		this.header = header;
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

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
