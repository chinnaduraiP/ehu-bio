package es.ehubio.proteomics;

import java.util.HashMap;
import java.util.Map;

public enum ScoreType {
	OTHER_LARGER(null,"other:larger","Other, larger values are better",true),
	OTHER_SMALLER(null,"other:smaller","Other, smaller values are better",false),
	MASCOT_EVALUE("MS:1001172","Mascot:expectation value","The Mascot result 'expectation value",false),
	MASCOT_SCORE("MS:1001171","Mascot:score","The Mascot result 'Score'",true),
	SEQUEST_XCORR("MS:1001155","SEQUEST:xcorr","The SEQUEST result 'XCorr'",true),
	XTANDEM_EVALUE("MS:1001330","X!Tandem:expect","The X!Tandem expectation value",false),
	XTANDEM_HYPERSCORE("MS:1001331","X!Tandem:hyperscore","The X!Tandem hyperscore",true),
	PROPHET_PROBABILITY(null,"PeptideProphet:probability","PeptideProphet probability score",true),
	PSM_P_VALUE("MS:1002352","PSM-level p-value","Estimation of the p-value for peptide spectrum matches",false),
	PSM_LOCAL_FDR("MS:1002351","PSM-level local FDR","Estimation of the local false discovery rate of peptide spectrum matches",false),
	PSM_Q_VALUE("MS:1002354","PSM-level q-value","Estimation of the q-value for peptide spectrum matches",false),
	PSM_FDR_SCORE("MS:1002355","PSM-level FDRScore","FDRScore for peptide spectrum matches",false),
	PEPTIDE_P_VALUE(null,"peptide-level p-value","Estimation of the p-value for peptides",false),
	PEPTIDE_LOCAL_FDR("MS:1002359","distinct peptide-level local FDR","Estimation of the local false discovery rate for distinct peptides once redundant identifications of the same peptide have been removed (id est multiple PSMs have been collapsed to one entry)",false),
	PEPTIDE_Q_VALUE(null,"peptide-level q-value","Estimation of the q-value for peptides",false),
	PEPTIDE_FDR_SCORE("MS:1002360","distinct peptide-level FDRScore","FDRScore for distinct peptides once redundant identifications of the same peptide have been removed (id est multiple PSMs have been collapsed to one entry)",false),
	PROTEIN_P_VALUE("MS:1001871","protein-level p-value","Estimation of the p-value for proteins",false),
	PROTEIN_LOCAL_FDR("MS:1002364","protein-level local FDR","Estimation of the local false discovery rate of proteins",false),
	PROTEIN_Q_VALUE("MS:1001869","protein-level q-value","Estimation of the q-value for proteins",false),
	GROUP_P_VALUE("MS:1002371","protein group-level p-value","Estimation of the p-value for protein groups",false),
	GROUP_LOCAL_FDR("MS:1002370","protein group-level local FDR","Estimation of the local false discovery rate of protein groups", false),
	GROUP_Q_VALUE("MS:1002373","protein group-level q-value","Estimation of the q-value for protein groups",false);
	
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