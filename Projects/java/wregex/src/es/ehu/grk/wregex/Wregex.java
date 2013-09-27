package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.grk.db.Fasta;

public final class Wregex {					
	public Wregex( String regex ) {
		mRegex = regex;
		mPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
	}

	public String getRegex() {
		return mRegex;
	}
	
	@Override
	public String toString() {
		return mRegex;
	}	
	
	public List<Result> search( Fasta fasta ) {
		List<Result> results = new ArrayList<Result>();
		Matcher matcher = mPattern.matcher(fasta.sequence());
		Matcher sub; // match all combinations
		String str;
		List<String> groups = new ArrayList<String>();
		int start = 0;
		while( matcher.find(start) ) {
			start = matcher.start()+1;
			str = matcher.group();
			for( int l = 1; l <= str.length(); l++ ) {
				sub = mPattern.matcher(str.substring(0, l));
				if( !sub.find() )
					continue;
				groups.clear();
				if( sub.groupCount() == 0 )
					groups.add(sub.group());
				else
					for( int g = 1; g <= sub.groupCount(); g++ )
						groups.add(sub.group(g));
				results.add(new Result(fasta, start+sub.start(), 1, sub.group(), groups));
			}
		}
		return results;
	}
	
	public List<ResultGroup> searchGrouping( Fasta fasta ) {
		List<ResultGroup> results = new ArrayList<ResultGroup>();		
		List<Result> list = search(fasta);
		if( list.isEmpty() )
			return results;
		List<Result> group = new ArrayList<Result>();
		boolean overlap;
		for( Result result : list ) {
			overlap = group.isEmpty();
			for( Result g : group ) {
				if( result.overlaps(g) ) {
					overlap = true;
					break;
				}
			}
			if( overlap ) {
				group.add(result);
				continue;
			}
			results.add(new ResultGroup(group));
			group = new ArrayList<Result>();
			group.add(result);
		}
		results.add(new ResultGroup(group));		
		return results;
	}
	
	private final String mRegex;
	private final Pattern mPattern;
}