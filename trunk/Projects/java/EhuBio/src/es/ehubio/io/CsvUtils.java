package es.ehubio.io;

public class CsvUtils {
	public static String getCsv( char separator, Object... fields) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for( ; i < fields.length - 1; i++ ) {
			builder.append(fields[i].toString());
			builder.append(separator);
		}
		builder.append(fields[i].toString());
		return builder.toString();
	}
}
