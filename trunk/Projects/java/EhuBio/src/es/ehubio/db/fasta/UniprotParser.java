package es.ehubio.db.fasta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniprotParser implements HeaderParser {
	@Override
	public boolean parse(String header) {
		if( uniProtPattern == null )
			uniProtPattern = Pattern.compile("^(..)\\|(\\S+)\\|(\\S+)");
		Matcher matcher = uniProtPattern.matcher(header);
		if( !matcher.find() )
			return false;
		
		accession = matcher.group(2);
		name = matcher.group(3);
		String[] fields = header.split("[ \\t]");
		for( int i = 0; i < fields.length; i++ )
			if( fields[i].contains("GN=") )
				geneName = fields[i].replaceAll("GN=", "");
		
		return true;
	}

	@Override
	public String getAccession() {
		return accession;
	}

	@Override
	public String getProteinName() {
		return name;
	}

	@Override
	public String getGeneName() {
		return geneName;
	}

	private static Pattern uniProtPattern;
	private String accession;
	private String name;
	private String geneName;
}
