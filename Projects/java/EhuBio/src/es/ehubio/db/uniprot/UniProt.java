package es.ehubio.db.uniprot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniProt {
	public static boolean validAccession( String acc ) {
		Matcher matcher = accPattern.matcher(acc);
		return matcher.matches();
	}
	
	public static String canonicalAccesion( String acc ) {
		return acc.replaceAll("NX_","").replaceAll("-.*", "");
	}
	
	public static String reducedAccession( String acc ) {
		return acc.replaceAll("NX_","").replaceAll("-1$", "");
	}
	
	protected static Pattern accPattern = Pattern.compile(
		"([A-NR-Z][0-9][A-Z][A-Z0-9][A-Z0-9][0-9])|([OPQ][0-9][A-Z0-9][A-Z0-9][A-Z0-9][0-9])");
}
