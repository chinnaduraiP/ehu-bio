package es.ehubio.proteomics.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class Digester {
	public static class Config {
		public Config(Enzyme enzyme) {
			this(enzyme, 0, false, 0);
		}
		public Config(Enzyme enzyme, int missedCleavages ) {
			this(enzyme, missedCleavages, false, 0);
		}
		public Config(Enzyme enzyme, int missedCleavages, boolean usingDP, int cutNterm) {
			this.enzyme = enzyme;
			pattern = !usingDP ? enzyme.getPattern() : Pattern.compile(
				String.format("(?:%s)|(?:%s)", enzyme.getRegex(), Enzyme.ASP_PRO.getRegex()),
				Pattern.CASE_INSENSITIVE);
			this.missedCleavages = missedCleavages;
			this.usingDP = usingDP;
			this.cutNterm = cutNterm;			
		}
		public Enzyme getEnzyme() {
			return enzyme;
		}
		public Pattern getPattern() {
			return pattern;
		}
		public int getMissedCleavages() {
			return missedCleavages;
		}
		// http://sourceforge.net/p/open-ms/tickets/580/
		// http://www.sigmaaldrich.com/life-science/custom-oligos/custom-peptides/learning-center/peptide-stability.html
		public boolean isUsingDP() {
			return usingDP;
		}
		// http://sourceforge.net/p/open-ms/tickets/580/
		public int getCutNterm() {
			return cutNterm;
		}		
		private final Enzyme enzyme;
		private final Pattern pattern;
		private final int missedCleavages;
		private final boolean usingDP;
		private final int cutNterm;
	}
	
	public static String[] digestSequence( String sequence, Pattern pattern ) {
		return pattern.split(sequence);
	}
	
	public static String[] digestSequence( String sequence, Enzyme enzyme ) {
		return digestSequence(sequence, enzyme.getPattern());
	}
	
	public static Set<String> digestSequence( String sequence, Pattern pattern, int missedCleavages ) {
		String[] orig = digestSequence(sequence, pattern);
		missedCleavages = Math.min(missedCleavages, orig.length-1);
		Set<String> list = new HashSet<>(Arrays.asList(orig));
		for( int i = 1; i <= missedCleavages; i++ )
			list.addAll(getMissed(orig,i));
		return list;		
	}
	
	public static Set<String> digestSequence( String sequence, Enzyme enzyme, int missedCleavages ) {
		return digestSequence(sequence, enzyme.getPattern(), missedCleavages);
	}
	
	private static List<String> getMissed(String[] orig, int num) {
		List<String> list = new ArrayList<>();
		int stop = orig.length-num;
		for( int i = 0; i < stop; i++ ) {
			StringBuilder str = new StringBuilder();
			for( int j = 0; j <= num; j++ )
				str.append(orig[i+j]);
			list.add(str.toString());
		}
		return list;
	}
	
	public static Set<String> digestSequence( String sequence, Config config ) {
		Set<String> list = digestSequence(sequence, config.getEnzyme(), config.getMissedCleavages());		
		if( config.isUsingDP() )
			for( String str : list.toArray(new String[0]) )
				list.addAll(digestSequence(str, Enzyme.ASP_PRO, 100));
		if( config.getCutNterm() > 0 && Character.toLowerCase(sequence.charAt(0))=='m' )		
			for( String str : list.toArray(new String[0]) )
				if( sequence.startsWith(str) )
					for( int cut = 1; cut <= config.getCutNterm(); cut++ )
						if( str.length() > cut )
							list.add(str.substring(cut));
		return list;		
	}

	public static Set<Peptide> digestDatabase( String path, Enzyme enzyme, int minLength ) throws IOException, InvalidSequenceException {
		List<Fasta> database = Fasta.readEntries(path, SequenceType.PROTEIN);
		Map<String,Peptide> peptides = new HashMap<>();
		for( Fasta fasta : database ) {
			Protein protein = new Protein();
			protein.setFasta(fasta);
			String[] seqs = digestSequence(protein.getSequence(), enzyme);
			for( String seq : seqs ) {
				if( seq.length() < minLength )
					continue;
				Peptide peptide = peptides.get(seq);
				if( peptide == null ) {
					peptide = new Peptide();
					peptide.setSequence(seq);
					peptides.put(seq, peptide);
				}
				peptide.addProtein(protein);
			}
		}
		return new HashSet<>(peptides.values());
	}
}
