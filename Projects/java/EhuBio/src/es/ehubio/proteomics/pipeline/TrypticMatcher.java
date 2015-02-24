package es.ehubio.proteomics.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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
	private TrypticMatcher( long decoys, long redundantDecoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, int maxMods, Aminoacid... varMods ) throws IOException, InvalidSequenceException {
		this.decoys = decoys;		
		//this.decoys = (long)Math.round(decoys*2.1);
		this.redundantDecoys = redundantDecoys;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.maxMods = maxMods;
		this.varMods = varMods;
	}
	
	public TrypticMatcher( String fastaPath, long decoys, long redundantDecoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, int maxMods, Aminoacid... varMods ) throws IOException, InvalidSequenceException {
		this(decoys,redundantDecoys,enzyme,missCleavages,minLength,maxLength,maxMods,varMods);
		loadCache(fastaPath);
		//System.out.println(String.format("%s - %s", decoys, total));
	}
	
	public TrypticMatcher( Collection<Protein> proteins, long decoys, long redundantDecoys, Enzyme enzyme, int missCleavages, int minLength, int maxLength, int maxMods, Aminoacid... varMods ) throws IOException, InvalidSequenceException {
		this(decoys,redundantDecoys,enzyme,missCleavages,minLength,maxLength,maxMods,varMods);		
		List<Fasta> fastas = new ArrayList<>();
		for( Protein protein : proteins ) {
			Fasta fasta = new Fasta(protein.getAccession(), protein.getDescription(), protein.getSequence(), SequenceType.PROTEIN);
			fastas.add(fasta);
		}
		createMq(fastas);
		//System.out.println(String.format("%s - %s", decoys, total));
	}
	
	private void loadCache( String fastaPath ) throws IOException, InvalidSequenceException {
		if( loadMq(getCacheName(fastaPath)) )
				return;
		List<Fasta> proteins = Fasta.readEntries(fastaPath, SequenceType.PROTEIN);
		createMq(proteins);
		saveMq(getCacheName(fastaPath));
	}
	
	private void saveMq( String cachePath ) throws IOException {
		logger.info("Saving Mq values for future uses ...");
		PrintWriter pw = new PrintWriter(Streams.getTextWriter(cachePath));		
		pw.println("Mq version:2.1.1");
		pw.println(String.format("enzyme:%s", enzyme.getDescription()));
		pw.println(String.format("missCleavages:%s", missCleavages));
		pw.println(String.format("minLength:%s", minLength));
		pw.println(String.format("maxLength:%s", maxLength));
		pw.println(String.format("maxMods:%s", maxMods));
		pw.println(getModString());
		for( Map.Entry<String, Result> entry : mapTryptic.entrySet() )
			pw.println(String.format("%s,%s,%s", entry.getKey(), entry.getValue().getNq(), entry.getValue().getMq()));
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

	private boolean loadMq( String cachePath ) throws IOException {
		File file = new File(cachePath);
		if( !file.exists() )
			return false;		
		
		totalNq = totalMq = 0.0;
		BufferedReader rd = new BufferedReader(Streams.getTextReader(file));
		if( "Mq version:2.1.1".equals(rd.readLine()) &&
			String.format("enzyme:%s", enzyme.getDescription()).equals(rd.readLine()) &&
			String.format("missCleavages:%s", missCleavages).equals(rd.readLine()) &&
			String.format("minLength:%s", minLength).equals(rd.readLine()) &&
			String.format("maxLength:%s", maxLength).equals(rd.readLine()) &&
			String.format("maxMods:%s", maxMods).equals(rd.readLine()) &&
			getModString().equals(rd.readLine()) ) {
			logger.info("Loading saved Mq values ...");
			String line;
			String[] fields;
			while( (line=rd.readLine()) != null ) {
				fields = line.split(",");
				double Nq = Double.parseDouble(fields[1]);
				double Mq = Double.parseDouble(fields[2]);
				totalNq += Nq;
				totalMq += Mq;
				mapTryptic.put(fields[0], new Result(Nq, Mq));
			}
		} else {
			logger.info("Discarded saved Mq values");
			rd.close();
			return false;
		}
		rd.close();
		return true;
	}
	
	private void createMq(List<Fasta> fastas) {
		List<Protein> proteins = digestDb(fastas);		
		totalNq = totalMq = 0.0;			
		for( Protein protein : proteins ) {
			double Mq = 0.0;
			double Nq = 0.0;
			for( Peptide peptide : protein.getPeptides() ) {
				if( peptide.getProteins().isEmpty() )
					throw new AssertionError("This should not happen");
				double tryptic =(double)getTryptic(peptide.getSequence());
				Nq += tryptic;
				Mq += tryptic/peptide.getProteins().size();				
			}
			totalNq += Nq;
			totalMq += Mq;
			mapTryptic.put(protein.getAccession(), new Result(Nq, Mq));			
		}		
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
	public Result getExpected(Protein protein) {
		Result tryptic = mapTryptic.get(protein.getAccession());
		return new Result(
			tryptic.getNq()/totalNq*redundantDecoys,
			tryptic.getMq()/totalMq*decoys
		);
	}
	
	private long getTryptic( String peptide ) {
		if( peptide.length() < minLength || peptide.length() > maxLength )
			return 0;
		if( varMods.length == 0 )
			return 1;
		int n = 0;
		for( Aminoacid aa : varMods )
			n += Math.min(countChars(peptide, aa), maxMods)+1;
		//return getCombinations(n);
		return n;
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
	
	/*private long getCombinations( int n ) {
		long result = 1;
		int kmax = n < maxMods ? n : maxMods;
		for( int k = 1; k <= kmax; k++ )			
			result += CombinatoricsUtils.binomialCoefficient(n, k);
		return result;
	}*/

	private final static Logger logger = Logger.getLogger(TrypticMatcher.class.getName());
	private final static int countWarning = 20;
	private final Enzyme enzyme;
	private final int missCleavages, minLength, maxLength, maxMods;
	private final Aminoacid[] varMods;	
	private double totalNq, totalMq;
	private final long decoys, redundantDecoys;
	private final Map<String, Result> mapTryptic = new HashMap<>();
}
