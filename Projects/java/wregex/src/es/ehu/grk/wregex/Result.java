package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.ehu.grk.db.Fasta;

/** Inmutable class for storing a Wregex result*/
public final class Result {
	private final String name;
	private final int start;
	private final int end;
	private final String match;
	private final List<String> groups;
	private final String alignment;
	private String printString = null;
	private final Fasta fasta;
	private double score = 0.0;
	private ResultGroup group = null;
	private int combinations = 0;
	
	Result( Fasta fasta, int start, String match, Collection<String> groups ) {
		assert groups.size() > 0;
		
		this.fasta = fasta;		
		this.start = start;
		this.end = start+match.length()-1;
		this.name = fasta.guessAccession()+"@"+start+"-"+end;		 		
		this.match = match;
		this.groups = new ArrayList<String>(groups);		
		
		StringBuilder builder = new StringBuilder();
		for( String str : groups )
			builder.append(str+"-");
		builder.deleteCharAt(builder.length()-1);
		this.alignment = builder.toString();				
	}
	
	void complete( ResultGroup group, double score ) {
		this.group = group;
		this.combinations = group == null ? 1 : this.group.getSize();
		this.score = score;
		this.printString = this.name + " (x" + getCombinations() + ") " + this.alignment + " score=" + score;
	}
	
	public String getName() {
		return name;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getCombinations() {
		return combinations;
	}

	public String getMatch() {
		return match;
	}

	public String getAlignment() {
		return alignment;
	}

	public Fasta getFasta() {
		return fasta;
	}

	public double getScore() {
		return score;
	}

	/** returns a defensive copy of the groups */
	public List<String> getGroups() {
		return new ArrayList<String>(groups);
	}
	
	@Override
	public String toString() {
		return printString;
	}
	
	public boolean overlaps(Result result) {
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
