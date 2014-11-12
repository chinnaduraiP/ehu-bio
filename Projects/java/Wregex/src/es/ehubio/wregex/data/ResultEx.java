package es.ehubio.wregex.data;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.io.CsvUtils;
import es.ehubio.wregex.Result;

public class ResultEx implements Comparable<ResultEx> {
	private final Result result;
	private int cosmicMissense = -1;
	private String cosmicUrl;
	private int dbPtms = -1;
	private String dbPtmUrl;
	private String motif;
	private String motifUrl;
	private String mutSequence;
	private Double mutScore;
	private static final char separator = ',';

	public int compareTo(ResultEx o) {
		// 1. COSMIC
		if( cosmicMissense > o.cosmicMissense )
			return -1;
		if( cosmicMissense < o.cosmicMissense )
			return 1;
		// 2. Wregex Score
		if( getScore() > o.getScore() )
			return -1;
		if( getScore() < o.getScore() )
			return 1;		
		// 3. PTMs
		if( dbPtms > o.dbPtms )
			return -1;
		if( dbPtms < o.dbPtms )
			return 1;
		// 4. Wregex Combinations
		if( getCombinations() > o.getCombinations() )
			return -1;
		if( getCombinations() < o.getCombinations() )
			return 1;
		// 5. Match length
		if( getMatch().length() > o.getMatch().length() )
			return -1;
		if( getMatch().length() < o.getMatch().length() )
			return 1;
		return 0;
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
	
	public static void saveCsv(Writer wr, List<ResultEx> results, boolean assays, boolean cosmic, boolean dbPtm ) {
		PrintWriter pw = new PrintWriter(wr);
		List<String> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(new String[]{"ID","Entry","Motif","Begin","End","Combinations","Sequence","Alignment","Score"}));
		if( assays ) { fields.add("Assay"); fields.add("Assay"); }
		if( cosmic ) { fields.add("Gene"); fields.add("COSMIC:Missense"); }
		if( dbPtm ) fields.add("dbPTM");
		pw.println(CsvUtils.getCsv(separator, fields.toArray()));
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
			if( dbPtm )
				fields.add(result.getDbPtmsAsString());
			pw.println(CsvUtils.getCsv(separator, fields.toArray()));
		}
		pw.flush();
	}
	
	private static List<Result> getResults( List<ResultEx> results ) {
		List<Result> list = new ArrayList<>();
		for( ResultEx result : results )
			list.add(result.result);
		return list;
	}
	
	public String getAccession() {
		if( result.getFasta().getAccession() == null )
			return "?";
		return result.getFasta().getAccession();
	}
	
	public String getGene() {
		if( result.getFasta().getGeneName() == null )
			return "?";
		return result.getFasta().getGeneName();
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

	public String getMotifUrl() {
		return motifUrl;
	}

	public void setMotifUrl(String motifUrl) {
		this.motifUrl = motifUrl;
	}

	public int getDbPtms() {
		return dbPtms;
	}
	
	public String getDbPtmsAsString() {
		return dbPtms < 0 ? "?" : ""+dbPtms;
	}

	public void setDbPtms(int dbPtms) {
		this.dbPtms = dbPtms;
	}

	public String getDbPtmUrl() {
		return dbPtmUrl;
	}

	public void setDbPtmUrl(String dbPtmUrl) {
		this.dbPtmUrl = dbPtmUrl;
	}

	public String getMutSequence() {
		return mutSequence;
	}

	public void setMutSequence(String mutSequence) {
		this.mutSequence = mutSequence;
	}

	public Double getMutScore() {
		return mutScore;
	}

	public void setMutScore(Double mutScore) {
		this.mutScore = mutScore;
	}
}