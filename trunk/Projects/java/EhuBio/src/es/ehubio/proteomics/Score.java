package es.ehubio.proteomics;

public class Score {
	private final ScoreType type;
	private final String name;
	private final double value;
	
	public Score( ScoreType type, String name, double value ) {
		this.type = type;
		this.name = name;
		this.value = value;
	}
	
	public Score( ScoreType type, double value ) {
		this.type = type;
		this.name = type.getName();
		this.value = value;
	}
	
	public ScoreType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public double getValue() {
		return value;
	}
	
	public int compare( double value2 ) {
		if( type.isLargerBetter() )
			value2 = value-value2;
		else
			value2 = value2-value;
		return (int)Math.signum(value2);
	}
}
