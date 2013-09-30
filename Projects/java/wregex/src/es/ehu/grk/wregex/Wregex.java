package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.grk.db.Aminoacid;
import es.ehu.grk.db.Fasta;
import es.ehu.grk.wregex.Pssm.PssmException;

public final class Wregex {
	public class WregexException extends Exception {
		private static final long serialVersionUID = 1L;

		public WregexException( String msg ) {
			super(msg);
		}
	}
	
	public Wregex( String regex, Pssm pssm ) throws WregexException {
		int check = regex.length()-regex.replaceAll("[()]", "").length();
		if( check == 0 && pssm != null )
			throw new WregexException("Regex groups must be defined for using a PSSM");
		else if( check%2 != 0 )
			throw new WregexException("Some regex groups are not closed");
		else if( pssm != null && check/2 != pssm.getGroups() )
			throw new WregexException("Provided regex and PSSM parameters are incompatible");
		mPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
		mPssm = pssm;
	}

	public String getRegex() {
		return mPattern.pattern();
	}
	
	public Pssm getPssm() {		
		return mPssm;
	}
	
	@Override
	public String toString() {
		if( mPssm == null )
			return mPattern.toString();
		return mPattern.toString()+"/PSSM";
	}	
	
	private List<Result> searchFirst( Fasta fasta ) {
		List<Result> results = new ArrayList<Result>();
		Matcher matcher = mPattern.matcher(fasta.sequence());
		Matcher sub; // match all combinations
		String str;
		List<String> groups = new ArrayList<String>();
		int start = 0;
		int end;
		while( matcher.find(start) ) {
			start = matcher.start()+1;
			str = matcher.group();
			end = str.length();
			sub = mPattern.matcher(str.substring(0, end));
			while( sub.find() && end > 0 ) {
				end = sub.end()-1;
				groups.clear();
				if( sub.groupCount() == 0 )
					groups.add(sub.group());
				else
					for( int g = 1; g <= sub.groupCount(); g++ )
						groups.add(sub.group(g));
				results.add(new Result(fasta, start+sub.start(), sub.group(), groups));
				sub = mPattern.matcher(str.substring(0, end));
			}
		}
		return results;
	}
	
	public List<ResultGroup> searchGrouping( Fasta fasta ) {
		// First search
		List<ResultGroup> results = new ArrayList<ResultGroup>();		
		List<Result> list = searchFirst(fasta);
		if( list.isEmpty() )
			return results;
		
		// Make groups
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
		
		// Complete results
		for( ResultGroup resultGroup : results )
			for( Result result : resultGroup )
				result.complete(resultGroup, getScore(result));
		
		return results;
	}
	
	public List<Result> search( Fasta fasta ) {
		List<ResultGroup> resultGroups = searchGrouping(fasta);
		List<Result> results = new ArrayList<>();
		for( ResultGroup group : resultGroups )
			for( Result result : group )
				results.add(result);
		return results;
	}
	
	double getScore(Result result) {
		double score = 0.0;
		if( mPssm == null )
			return score;
		List<String> groups = result.getGroups();
		String group;
		for( int g = 0; g < groups.size(); g++ ) {
			group = groups.get(g);
			for( int i = 0; i < group.length(); i++ ) {
				try {
					score += mPssm.getScore(Aminoacid.parseLetter(group.charAt(i)),g);
				} catch (PssmException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return Math.pow(10.0,score/result.getMatch().length())*100.0;
	}
	
	private final Pattern mPattern;
	private final Pssm mPssm;
}