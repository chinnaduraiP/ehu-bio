package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.Protein;

public class TrypticMatcher implements RandomMatcher {
	public TrypticMatcher( Collection<Protein> proteins, long decoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, Aminoacid... varMods ) {
		this.decoys = decoys;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.varMods = varMods;
		long total = 0;
		
		for( Protein protein : proteins )
			total += getTryptic(protein);
		this.total = total;
						
	}

	@Override
	public double getNq(Protein protein) {
		double Nq = getTryptic(protein)/((double)total)*decoys;
		if( Nq < minNq )
			Nq = 1.0;
		return Nq;
	}
	
	private long getTryptic( Protein protein ) {
		List<String> list = Digester.digestSequence(protein.getSequence(), enzyme, missCleavages);
		long count = 0;
		for( String peptide : list ) {
			if( peptide.length() < minLength || peptide.length() > maxLength )
				continue;
			for( Aminoacid aa : varMods )
				count += getCombinations(countChars(peptide, aa)); 
		}
		return count;
	}
	
	private long countChars( String seq, Aminoacid aa ) {
		char ch = Character.toUpperCase(aa.letter);
		char[] chars = seq.toUpperCase().toCharArray();
		long count = 0;
		for( int i = 0; i < chars.length; i++ )
			if( chars[i] == ch )
				count++;
		if( count >= countWarning )
			logger.warning(String.format("%s,%s",count,seq));
		return count;
	}
	
	private long getCombinations( long n ) {
		long result = 1;
		for(;n>0;n--)
			result *= 2;
		return result;
	}

	private final static Logger logger = Logger.getLogger(TrypticMatcher.class.getName());
	private final static int countWarning = 20;
	private final static double minNq = 0.1;
	private final Enzyme enzyme;
	private final int missCleavages, minLength, maxLength;
	private final Aminoacid[] varMods;	
	private final long total;
	private final long decoys;
}
