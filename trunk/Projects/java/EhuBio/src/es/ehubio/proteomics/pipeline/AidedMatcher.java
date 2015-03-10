package es.ehubio.proteomics.pipeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.io.CsvReader;
import es.ehubio.io.Streams;
import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class AidedMatcher implements RandomMatcher {
	public AidedMatcher(String relationsPath, double decoys, double redundantDecoys, int maxMods, Aminoacid... varMods) throws FileNotFoundException, IOException {
		this.decoys = decoys;
		this.redundantDecoys = redundantDecoys;
		this.maxMods = maxMods;
		this.varMods = varMods;
		createMq(loadProteins(relationsPath));
	}
	
	private Collection<Protein> loadProteins(String relationsPath) throws FileNotFoundException, IOException {
		logger.info("Loading peptides used in the search engine ...");
		Map<String,Protein> map = new HashMap<>();
		CsvReader csv = new CsvReader(" ", false, true);
		csv.open(Streams.getTextReader(relationsPath));
		while( csv.readLine() != null ) {
			Peptide peptide = new Peptide();
			peptide.setUniqueString(csv.getField(0));
			peptide.setSequence(csv.getField(0).replaceAll("\\[.*?\\]",""));
			for( String acc : csv.getField(1).split(";") ) {
				Protein protein = map.get(acc);
				if( protein == null ) {
					protein = new Protein();
					protein.setAccession(acc);
					map.put(acc, protein);
				}
				protein.addPeptide(peptide);
			}
		}
		csv.close();
		return map.values();
	}
	
	private void createMq( Collection<Protein> proteins ) {
		totalNq = totalMq = 0;
		for( Protein protein : proteins ) {
			double Mq = 0.0;
			double Nq = 0.0;
			Set<String> peptides = new HashSet<>();
			for( Peptide peptide : protein.getPeptides() ) {
				if( !peptides.add(peptide.getSequence().toLowerCase()) )
					continue;
				double tryptic = (double)getTryptic(peptide.getSequence());
				Nq += tryptic;
				Mq += tryptic/peptide.getProteins().size();				
			}
			totalNq += Nq;
			totalMq += Mq;
			results.put(protein.getAccession(), new Result(Nq, Mq));
		}
	}
	
	private long getTryptic( String peptide ) {
		if( varMods.length == 0 )
			return 1;
		int n = 0;
		for( Aminoacid aa : varMods )
			n += Math.min(countChars(peptide, aa), maxMods)+1;
		return n;
	}
	
	private long countChars( String seq, Aminoacid aa ) {
		char ch = Character.toUpperCase(aa.letter);
		char[] chars = seq.toUpperCase().toCharArray();
		long count = 0;
		for( int i = 0; i < chars.length; i++ )
			if( chars[i] == ch )
				count++;
		return count;
	}

	@Override
	public Result getExpected(Protein protein) {
		Result tryptic = results.get(protein.getAccession());
		return new Result(
			tryptic.getNq()/totalNq*redundantDecoys,
			tryptic.getMq()/totalMq*decoys
		);
	}

	private final static Logger logger = Logger.getLogger(AidedMatcher.class.getName());
	private final Map<String, Result> results = new HashMap<>();
	private double totalNq, totalMq;
	private final double decoys, redundantDecoys;
	private final int maxMods;
	private final Aminoacid[] varMods;
}
