package es.ehubio.wregex;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import es.ehubio.db.Fasta;

/** Inmutable class for storing a Wregex result*/
public final class Result implements Comparable<Result> {
	private final String name;
	private final int start;
	private final int end;
	private final String match;
	private final List<String> groups;
	private final String alignment;
	private String printString = null;
	private final Fasta fasta;
	private double score = 0.0;
	private double assay = -1.0;
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
	
	public String getEntry() {
		return fasta.guessAccession();
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
	
	public String getScoreAsString() {
		return String.format("%.1f", getScore());
	}
	
	void setAssay(double assay) {
		this.assay = assay;
	}
	
	public double getAssay() {
		return assay;
	}		
	
	public String getAssayAsString() {		
		if( assay < 0 )
			return "?";
		return String.format("%.1f", getAssay());
		/*if( assay < 0.5 )
			return "negative";		
		return ((int)(assay/10.0+0.5))+"+";*/
	}
	
	public double getGroupAssay() {
		return group.getAssay();
	}
	
	public String getGroupAssayAsString() {
		return group.getAssayAsString();
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

	@Override
	public int compareTo(Result o) {
		if( score > o.score )
			return -1;
		if( score < o.score )
			return 1;
		if( combinations > o.combinations )
			return -1;
		if( combinations < o.combinations )
			return 1;
		return 0;
	}
	
	public static void saveCsv(Writer wr, List<Result> results) {
		PrintWriter pw = new PrintWriter(wr);
		pw.println("ID,Entry,Begin,End,Combinations,Sequence,Alignment,Score");
		for( Result result : results )
			pw.println(result.getName()+","+result.getEntry()+","+result.getStart()+","+result.getEnd()+","+result.getCombinations()+","+result.getMatch()+","+result.getAlignment()+","+result.getScore());
		pw.flush();
	}
	
	public static void saveAln(Writer wr, List<Result> results) {
		PrintWriter pw = new PrintWriter(wr);
		int groups = results.get(0).getGroups().size();
		int[] sizes = new int[groups];
		int first = 0, i;
		
		// Calculate lengths for further alignment
		for( i = 0; i < groups; i++ )
			sizes[i] = 0;
		for( Result result : results ) {
			if( result.getName().length() > first )
				first = result.getName().length();
			for( i = 0; i < groups; i++ )
				if( result.getGroups().get(i).length() > sizes[i] )
					sizes[i] = result.getGroups().get(i).length(); 
		}
		
		// Write ALN
		pw.println("CLUSTAL 2.1 multiple sequence alignment (by WREGEX)\n\n");
		for( Result result : results ) {
			pw.print(StringUtils.rightPad(result.getName(), first+4));
			for( i = 0; i < groups; i++ )
				pw.print(StringUtils.rightPad(result.getGroups().get(i),sizes[i],'-'));
			pw.println();
		}
		pw.println();
		pw.flush();
	}
	
	public static void saveAssay(Writer wr, List<Result> results, boolean grouping) {
		PrintWriter pw = new PrintWriter(wr);
		pw.println("ID,Entry,Begin,End,Combinations,Sequence,Alignment,Score,Assay,Assay");
		String assay;
		for( Result result : results ) {
			if( grouping )
				assay = result.getGroupAssayAsString()+","+result.getGroupAssay();
			else
				assay = result.getAssayAsString()+","+result.getAssay();
			pw.println(result.getName()+","+result.getEntry()+","+result.getStart()+","+result.getEnd()+","+result.getCombinations()+","+result.getMatch()+","+result.getAlignment()+","+result.getScore()+","+assay);
		}
		pw.flush();
	}
}
