package es.ehubio.proteomics;

public enum ScoreType {
	LARGER("Other, larger values are better", true),
	SMALLER("Other, smaller values are better",false),
	MASCOT_EVALUE("Mascot expectation value",false),
	MASCOT_SCORE("Mascot score",true),
	SEQUEST_XCORR("SEQUEST Confidence XCorr",true),
	XTANDEM_EVALUE("X!Tandem expect",false),
	XTANDEM_HYPERSCORE("X!Tandem hyperscore",true),
	PROPHET_PROBABILITY("PeptideProphet probability score",true),
	MZID_PASS_THRESHOLD("mzIdentML SpectrumIdentificationItem passThreshold attribute",true);
	
	private final String name;
	private final boolean largerBetter;
	
	private ScoreType( String name, boolean largerBetter ) {
		this.name = name;
		this.largerBetter = largerBetter;
	}
	
	public String getName() {
		return name;
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
}