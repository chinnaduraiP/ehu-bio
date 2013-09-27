package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.ehu.grk.db.Fasta;

/** Inmutable class for storing a Wregex result*/
public final class Result {
	public final String name;
	public final int start;
	public final int end;
	public final int combinations;
	public final String match;
	private final List<String> groups;
	public final String alignment;
	private final String printString;
	public final Fasta fasta;	
	
	Result( Fasta fasta, int start, int combinations, String match, Collection<String> groups ) {
		assert groups.size() > 0;
		
		this.fasta = fasta;		
		this.start = start;
		this.end = start+match.length()-1;
		this.name = fasta.guessAccession()+"@"+start+"-"+end;
		this.combinations = combinations;
		this.match = match;
		this.groups = new ArrayList<String>(groups);
		
		StringBuilder builder = new StringBuilder();
		for( String str : groups )
			builder.append(str+"-");
		builder.deleteCharAt(builder.length()-1);
		this.alignment = builder.toString();
		
		this.printString = this.name + " (x" + combinations + ") " + this.alignment; 
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
