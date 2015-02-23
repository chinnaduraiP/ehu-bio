package es.ehubio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
	public static boolean compare( Object o1, Object o2 ) {
		if( o1 == null && o2 == null )
			return true;
		if( o1 == null && o2 != null )
			return false;
		if( o1 != null && o2 == null )
			return false;
		return o1.equals(o2);
	}
	
	public static int hashCode( Object o1, Object o2 ) {
		int hash = 7;
		if( o1 != null )
			hash = 29*hash + o1.hashCode();
		if( o2 != null )
			hash = 29*hash + o2.hashCode();
		return hash;
	}
	
	public static String mergeStrings( Set<String> strings ) {
		// Canonical cases
		if( strings.isEmpty() )
			return null;
		if( strings.size() == 1 )
			return strings.iterator().next();

		// Sort strings
		List<String> list = new ArrayList<>(strings);
		Collections.sort(list);

		// Merge strings
		if( mergePattern == null )
			mergePattern = Pattern.compile("[0-9a-zA-Z]+");
		Matcher matcher = mergePattern.matcher(list.get(0));
		if( !matcher.find() )
			return null;
		String base = matcher.group();
		StringBuilder name = new StringBuilder(base);
		boolean wildcard = false;
		for( int i = 1; i < list.size(); i++ ) {
			matcher = mergePattern.matcher(list.get(i));
			if( !matcher.find() )
				return null;
			String next = matcher.group();
			if( next.equals(base) ) {
				if( !wildcard ) {
					name.append('*');
					wildcard = true;
				}
			} else {
				base = next;
				name.append('+');
				name.append(base);
				wildcard = false;
			}
		}
		return name.toString();
	}
	
	public static double median( List<Double> list ) {
		Collections.sort(list);
		int i = list.size()/2;
		if( list.size()%2 == 1 )
			return list.get(i);
		return (list.get(i)+list.get(i+1))/2;
	}
	
	private static Pattern mergePattern;
}