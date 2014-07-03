package es.ehubio.proteomics;

import java.util.HashMap;
import java.util.Map;

public enum ScoreType {
	OTHER_LARGER(null,"Other:larger","Other, larger values are better",true),
	OTHER_SMALLER(null,"Other:smaller","Other, smaller values are better",false),
	MASCOT_EVALUE("MS:1001172","Mascot:expectation value","The Mascot result 'expectation value",false),
	MASCOT_SCORE("MS:1001171","Mascot:score","The Mascot result 'Score'",true),
	SEQUEST_XCORR("MS:1001155","SEQUEST:xcorr","The SEQUEST result 'XCorr'",true),
	XTANDEM_EVALUE("MS:1001330","X!Tandem:expect","The X!Tandem expectation value",false),
	XTANDEM_HYPERSCORE("MS:1001331","X!Tandem:hyperscore","The X!Tandem hyperscore",true),
	PROPHET_PROBABILITY(null,"PeptideProphet:probability","PeptideProphet probability score",true),
	PSM_LOCAL_FDR("MS:1002351","PSM-level local FDR","Estimation of the local false discovery rate of peptide spectrum matches",false),
	PSM_Q_VALUE("MS:1002354","PSM-level q-value","Estimation of the q-value for peptide spectrum matches",false),
	PSM_FDR_SCORE("MS:1002355","PSM-level FDRScore","FDRScore for peptide spectrum matches",false);
	
	private final String accession;
	private final String name;	
	private final String description;
	private final boolean largerBetter;
	
	private final static Map<String,ScoreType> mapAccession = new HashMap<>();	
	private final static Map<String,ScoreType> mapName = new HashMap<>();
	static {
		for( ScoreType type : ScoreType.values() ) {
			if( type.getAccession() != null )
				mapAccession.put(type.getAccession(), type);
			if( type.getName() != null )
				mapName.put(type.getName(), type);
		}
	}
	
	private ScoreType( String accession, String name, String description, boolean largerBetter ) {		
		this.accession = accession;
		this.name = name;
		this.description = description;
		this.largerBetter = largerBetter;
	}
	
	public String getAccession() {
		return accession;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public boolean isLargerBetter() {
		return largerBetter;
	}
	
	public int compare( double v1, double v2 ) {
		if( isLargerBetter() )
			v2 = v1-v2;
		else
			v2 = v2-v1;
		return (int)Math.signum(v2);
	}
	
	public static ScoreType getByAccession( String accession ) {
		return mapAccession.get(accession);
	}
	
	public static ScoreType getByName( String name ) {
		return mapName.get(name);
	}
}