package es.ehubio.wregex.view;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.ehubio.Utils;
import es.ehubio.db.Fasta;
import es.ehubio.wregex.Result;

public class ResultEx implements Comparable<ResultEx> {
	private final Result result;
	private int cosmicMissense = -1;
	private String cosmicUrl;
	private String motif;
	private static final char separator = ',';

	public int compareTo(ResultEx o) {
		int cmp = 0;
		if( cosmicMissense != -1 || o.cosmicMissense != -1 ) {
			if( cosmicMissense > o.cosmicMissense )
				cmp = -1;
			else if( cosmicMissense < o.cosmicMissense )
				cmp = 1;
		}
		if( cmp == 0 )
			cmp = result.compareTo(o.result);
		return cmp;
	}

	public boolean equals(Object obj) {
		return result.equals(obj);
	}

	public String getAlignment() {
		return result.getAlignment();
	}

	public double getAssay() {
		return result.getAssay();
	}
	
	private String assayToString( double assay ) {
		if( assay < 0 )
			return "?";
		return String.format("%.1f", assay);
		/*if( assay < 0.5 )
			return "negative";		
		return ((int)(assay/10.0+0.5))+"+";*/
	}

	public String getAssayAsString() {		
		return assayToString(getAssay());
	}

	public int getCombinations() {
		return result.getCombinations();
	}

	public int getEnd() {
		return result.getEnd();
	}

	public String getEntry() {
		return result.getEntry();
	}

	public Fasta getFasta() {
		return result.getFasta();
	}

	public double getGroupAssay() {
		return result.getGroupAssay();
	}

	public String getGroupAssayAsString() {
		return assayToString(getGroupAssay());
	}

	public List<String> getGroups() {
		return result.getGroups();
	}

	public String getMatch() {
		return result.getMatch();
	}

	public String getName() {
		return result.getName();
	}

	public double getScore() {
		return result.getScore();
	}

	public String getScoreAsString() {
		if( getScore() < 0.0 )
			return "?";
		return String.format("%.1f", getScore());
	}

	public int getStart() {
		return result.getStart();
	}

	public int hashCode() {
		return result.hashCode();
	}

	public boolean overlaps(Result result) {
		return result.overlaps(result);
	}

	public String toString() {
		return result.toString();
	}

	public ResultEx( Result result ) {
		this.result = result;
	}
	
	public static void saveAln(Writer wr, List<ResultEx> results) {
		Result.saveAln(wr, getResults(results)); 		
	}
	
	public static void saveCsv(Writer wr, List<ResultEx> results, boolean assays, boolean cosmic ) {
		PrintWriter pw = new PrintWriter(wr);
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(new String[]{"ID","Entry","Motif","Begin","End","Combinations","Sequence","Alignment","Score"}));
		if( assays ) { fields.add("Assay"); fields.add("Assay"); }
		if( cosmic ) { fields.add("Gene"); fields.add("COSMIC:Missense"); }
		pw.println(Utils.getCsv(separator, fields.toArray()));
		for( ResultEx result : results ) {
			fields.clear();			
			fields.add(result.getName());
			fields.add(result.getEntry());
			fields.add(result.getMotif());
			fields.add(""+result.getStart());
			fields.add(""+result.getEnd());
			fields.add(""+result.getCombinations());
			fields.add(result.getMatch());
			fields.add(result.getAlignment());
			fields.add(""+result.getScore());
			if( assays ) {
				fields.add(result.getGroupAssayAsString());
				fields.add(""+result.getGroupAssay());
			}
			if( cosmic ) {
				fields.add(result.getGene());
				fields.add(result.getCosmicMissenseAsString());
			}
			pw.println(Utils.getCsv(separator, fields.toArray()));
		}
		pw.flush();
	}
	
	private static List<Result> getResults( List<ResultEx> results ) {
		List<Result> list = new ArrayList<>();
		for( ResultEx result : results )
			list.add(result.result);
		return list;
	}
	
	public String getGene() {
		if( result.getFasta().guessGene() == null )
			return "?";
		return result.getFasta().guessGene();
	}

	public int getCosmicMissense() {
		return cosmicMissense;
	}
	
	public String getCosmicMissenseAsString() {
		return cosmicMissense < 0 ? "?" : ""+cosmicMissense;
	}

	public void setCosmicMissense(int cosmicMutations) {
		this.cosmicMissense = cosmicMutations;
	}

	public String getCosmicUrl() {
		return cosmicUrl;
	}

	public void setCosmicUrl(String cosmicUrl) {
		this.cosmicUrl = cosmicUrl;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}
}