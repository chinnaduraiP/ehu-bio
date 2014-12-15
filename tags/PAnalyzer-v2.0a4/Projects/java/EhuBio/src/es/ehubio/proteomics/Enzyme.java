package es.ehubio.proteomics;

import java.util.regex.Pattern;

public enum Enzyme {
	TRYPSIN("Trypsin","Trypsin (C-term to K/R, except before P)","(?<=[KR])(?!P)"),
	TRYPSIN_P("Trypsin-P","Trypsin (C-term to K/R, even before P)","(?<=[KR])");
	
	private final String name;
	private final String description;
	private final String regex;
	private final Pattern pattern;
	
	private Enzyme( String name, String description, String regex) {
		this.name = name;
		this.description = description;
		this.regex = regex;
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	public String getRegex() {
		return regex;
	}

	public String getDescription() {
		return description;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}