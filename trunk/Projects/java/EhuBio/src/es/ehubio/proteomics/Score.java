package es.ehubio.proteomics;

public class Score {
	private ScoreType type;
	private String name;
	private double value;
	
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
	
	public void setType(ScoreType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public int compare( double value2 ) {
		return type.compare(value, value2);
	}
	
	@Override
	public String toString() {
		return String.format("%s=%s", getName(), getValue());
	}
}
