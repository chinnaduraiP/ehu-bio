package es.ehubio.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehubio.io.UnixCfgReader;

public final class Fasta {
	private final String mSequence;
	private final String mHeader;
	private final SequenceType mType;
	private final String mGuessedAccession;
	private final String mGuessedName;
	private final String mGuessedGene;
	private static Pattern uniProtPattern;
	private static Pattern headerPattern;
	
	public enum SequenceType {
		PROTEIN, DNA, RNA
	}
	
	public static class InvalidSequenceException extends Exception {
		private static final long serialVersionUID = 1L;
		public InvalidSequenceException() {
			super("Illegal character found in fasta sequence");
		}
		public InvalidSequenceException( String desc ) {
			super(desc);
		}
		public InvalidSequenceException( char ch ) {
			super("Illegal character '" + ch + "' found in fasta sequence");
		}
	}
	
	public Fasta( String header, String sequence, SequenceType type ) throws InvalidSequenceException {
		assert header != null && sequence != null;
		mHeader = header;
		mSequence = sequence.trim().replaceAll("[ \t]", "");		
		mType = type;
		checkSequence(mSequence, type);
		
		// Fields in header identifier
		mGuessedName = header.split("[ \t]")[0];
		if( uniProtPattern == null )
			uniProtPattern = Pattern.compile("^(..)\\|(\\w+)\\|(\\w+)");
		Matcher matcher = uniProtPattern.matcher(header);
		String accession = null;
		if( matcher.find() )
			accession = matcher.group(2);
		mGuessedAccession = accession;
		
		// Fields in header description
		if( headerPattern == null )
			headerPattern = Pattern.compile("(..)=(\\w+)");
		matcher = headerPattern.matcher(header);
		String gene = null;
		while( matcher.find() )
			if( matcher.group(1).equalsIgnoreCase("GN") ) {
				gene = matcher.group(2);
				break;
			}
		mGuessedGene = gene;
	}
	
	public static void checkSequence( String sequence, SequenceType type ) throws InvalidSequenceException {
		if( sequence.isEmpty() )
			throw new InvalidSequenceException("Empty sequence");
		List<Character> chars = new ArrayList<Character>();
		switch( type ) {
			case DNA:
				for( Nucleotide n : Nucleotide.values() )
					if( n.isDNA ) chars.add(Character.toUpperCase(n.symbol));
				break;			
			case RNA:
				for( Nucleotide n : Nucleotide.values() )
					if( n.isRNA ) chars.add(Character.toUpperCase(n.symbol));
				break;
			case PROTEIN:
				for( Aminoacid a : Aminoacid.values() )
					chars.add(Character.toUpperCase(a.letter));
				break;
		}
		for( char c : sequence.toUpperCase().toCharArray() )
			if( !chars.contains(c) )
				new InvalidSequenceException(c);
	}
	
	public String sequence() {
		return mSequence;
	}
	
	public String header() {
		return mHeader;
	}
	
	public SequenceType type() {
		return mType;
	}
	
	public String guessAccession() {
		return mGuessedAccession;
	}
	
	public String guessName() {
		return mGuessedName;
	}
	
	public String guessGene() {
		return mGuessedGene;
	}
	
	public static List<Fasta> readEntries( Reader rd, SequenceType type ) throws IOException, InvalidSequenceException {
		List<Fasta> list = new ArrayList<Fasta>();
		UnixCfgReader br = new UnixCfgReader(rd);
		String line, header = null;
		StringBuilder sequence = new StringBuilder();
		while( (line=br.readLine()) != null ) {
			if( line.startsWith(">") ) {
				if( header != null )
					list.add(new Fasta(header, sequence.toString(), type));
				header = line.substring(1).trim();
				sequence = new StringBuilder();					
			} else
				sequence.append(line);
		}
		if( header != null )
			list.add(new Fasta(header, sequence.toString(), type));
		return list;
	}
	
	public static void writeEntries( Writer wr, Iterable<Fasta> list) {
		PrintWriter pw = new PrintWriter(wr);
		for( Fasta f : list ) {
			pw.println(">" + f.header());
			pw.println(f.sequence());
		}
		pw.flush();
	}	
}
