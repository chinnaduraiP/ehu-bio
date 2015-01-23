package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class TrypticMatcher implements RandomMatcher {
	public TrypticMatcher( Collection<Protein> proteins, boolean shared, long decoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, Aminoacid... varMods ) {
		this.decoys = decoys;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.varMods = varMods;
		this.total = shared ? createMq(proteins) : createNq(proteins);
	}
	
	private long createNq(Collection<Protein> proteins) {
		long total = 0;		
		for( Protein protein : proteins ) {
			List<String> list = Digester.digestSequence(protein.getSequence(), enzyme, missCleavages);
			long tryptic = 0;
			for( String peptide : list )
				tryptic += getTryptic(peptide);
			total += tryptic;
			mapTryptic.put(protein.getAccession(), (double)tryptic);
		}
		return total;
	}
	
	private long createMq(Collection<Protein> proteins) {
		List<Protein> list = digestDb(proteins);
		double total = 0.0;			
		for( Protein protein : list ) {
			double tryptic = 0.0;
			for( Peptide peptide : protein.getPeptides() ) {
				tryptic += ((double)getTryptic(peptide.getSequence()))/peptide.getProteins().size();
				total += tryptic;
			}
			mapTryptic.put(protein.getAccession(), tryptic);
		}
		return Math.round(total);
	}
	
	private List<Protein> digestDb(Collection<Protein> proteins) {
		Map<String,Peptide> mapPeptides = new HashMap<>();
		List<Protein> list = new ArrayList<>();		
		for( Protein protein : proteins ) {
			List<String> pepSequences = Digester.digestSequence(protein.getSequence(), enzyme, missCleavages);
			Protein protein2 = new Protein();
			protein2.setAccession(protein.getAccession());
			for( String pepSequence : pepSequences ) {
				Peptide peptide = mapPeptides.get(pepSequence);
				if( peptide == null ) {
					peptide = new Peptide();
					peptide.setSequence(pepSequence);
					mapPeptides.put(pepSequence, peptide);
				}				
				protein2.addPeptide(peptide);
			}
			list.add(protein2);
		}		
		return list;
	}

	@Override
	public double getExpected(Protein protein) {
		double Nq = mapTryptic.get(protein.getAccession())/((double)total)*decoys;
		if( Nq < minNq )
			Nq = 1.0;
		return Nq;
	}
	
	private long getTryptic( String peptide ) {
		if( peptide.length() < minLength || peptide.length() > maxLength )
			return 0;
		long count = 0;
		for( Aminoacid aa : varMods )
			count += getCombinations(countChars(peptide, aa));
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
	private final Map<String, Double> mapTryptic = new HashMap<>();
}
