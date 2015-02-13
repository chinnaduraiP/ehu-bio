package es.ehubio.proteomics.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.io.Streams;
import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class TrypticMatcher implements RandomMatcher {
	public TrypticMatcher( String fastaPath, boolean shared, long decoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, Aminoacid... varMods ) throws IOException, InvalidSequenceException {
		this.decoys = decoys;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.varMods = varMods;
		this.total = loadCache(fastaPath, shared);
		//System.out.println(String.format("%s - %s", decoys, total));
	}
	
	private long loadCache( String fastaPath, boolean shared ) throws IOException, InvalidSequenceException {
		long total;
		if( shared ) {
			total = loadMq(getCacheName(fastaPath));
			if( total >= 0 )
				return total;
		}
		List<Fasta> proteins = Fasta.readEntries(fastaPath, SequenceType.PROTEIN);
		if( shared ) {
			total = createMq(proteins);
			saveMq(getCacheName(fastaPath));
		} else
			total = createNq(proteins);
		return total;
	}
	
	private void saveMq( String cachePath ) throws IOException {
		logger.info("Saving Mq values for future uses ...");
		PrintWriter pw = new PrintWriter(Streams.getTextWriter(cachePath));		
		pw.println("Mq version:1.0");
		pw.println(String.format("enzyme:%s", enzyme.name()));
		pw.println(String.format("missCleavages:%s", missCleavages));
		pw.println(String.format("minLength:%s", minLength));
		pw.println(String.format("maxLength:%s", maxLength));
		pw.println(getModString());
		for( Map.Entry<String, Double> entry : mapTryptic.entrySet() )
			pw.println(entry.getKey()+","+entry.getValue());
		pw.close();
	}
	
	private String getModString() {
		StringBuilder str = new StringBuilder();
		str.append("varMods:");
		for( Aminoacid aa : varMods )
			str.append(aa.letter);
		return str.toString();
	}
	
	private String getCacheName( String fastaPath ) {
		return fastaPath.replaceAll("\\.fasta(\\.gz)?$", ".Mq.gz");
	}

	private long loadMq( String cachePath ) throws IOException {
		File file = new File(cachePath);
		if( !file.exists() )
			return -1;		
		
		double total = 0.0;
		BufferedReader rd = new BufferedReader(Streams.getTextReader(file));
		if( "Mq version:1.0".equals(rd.readLine()) &&
			String.format("enzyme:%s", enzyme.name()).equals(rd.readLine()) &&
			String.format("missCleavages:%s", missCleavages).equals(rd.readLine()) &&
			String.format("minLength:%s", minLength).equals(rd.readLine()) &&
			String.format("maxLength:%s", maxLength).equals(rd.readLine()) &&
			getModString().equals(rd.readLine()) ) {
			logger.info("Loading saved Mq values ...");
			String line;
			String[] fields;
			while( (line=rd.readLine()) != null ) {
				fields = line.split(",");
				double tryptic = Double.parseDouble(fields[1]);
				total += tryptic;
				mapTryptic.put(fields[0], tryptic);
			}
		} else {
			logger.info("Discarded saved Mq values");
			rd.close();
			return -1;
		}
		rd.close();
		return Math.round(total);
	}
	
	private long createNq(List<Fasta> proteins) {
		logger.info("Computing observable peptides ...");
		long total = 0;		
		for( Fasta protein : proteins ) {
			List<String> list = Digester.digestSequence(protein.getSequence(), enzyme, missCleavages);
			long tryptic = 0;
			for( String peptide : list )
				tryptic += getTryptic(peptide);
			total += tryptic;
			mapTryptic.put(protein.getAccession(), (double)tryptic);
		}
		return total;
	}
	
	private long createMq(List<Fasta> proteins) {
		List<Protein> list = digestDb(proteins);
		double total = 0.0;			
		for( Protein protein : list ) {
			double tryptic = 0.0;
			for( Peptide peptide : protein.getPeptides() ) {
				if( peptide.getProteins().isEmpty() )
					throw new AssertionError("This should not happen"); 
				tryptic += ((double)getTryptic(peptide.getSequence()))/peptide.getProteins().size();				
			}
			total += tryptic;
			mapTryptic.put(protein.getAccession(), tryptic);
		}
		return Math.round(total);
	}
	
	private List<Protein> digestDb(List<Fasta> proteins) {
		logger.info("Building a dataset with all observable peptides ...");
		Map<String,Peptide> mapPeptides = new HashMap<>();
		List<Protein> list = new ArrayList<>();		
		for( Fasta protein : proteins ) {
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
		return mapTryptic.get(protein.getAccession())/((double)total)*decoys;
	}
	
	private long getTryptic( String peptide ) {
		if( peptide.length() < minLength || peptide.length() > maxLength )
			return 0;
		if( varMods.length == 0 )
			return 1;
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
	private final Enzyme enzyme;
	private final int missCleavages, minLength, maxLength;
	private final Aminoacid[] varMods;	
	private final long total;
	private final long decoys;
	private final Map<String, Double> mapTryptic = new HashMap<>();
}
