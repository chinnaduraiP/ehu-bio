package es.ehu.grk.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import es.ehu.grk.io.UnixCfgReader;

public final class Fasta {
	private final String mSequence;
	private final String mHeader;
	private final SequenceType mType;
	private final String mGuessedAccession;
	
	public enum SequenceType {
		PROTEIN, DNA, RNA
	}
	
	public class InvalidSequenceException extends Exception {
		private static final long serialVersionUID = 1L;
		public InvalidSequenceException() {
			super("Illegal character found in fasta sequence");
		}
		public InvalidSequenceException( String desc ) {
			super(desc);
		}
	}
	
	public Fasta( String header, String sequence, SequenceType type ) throws InvalidSequenceException {
		assert header != null && sequence != null;
		mHeader = header;
		mSequence = sequence;		
		mType = type;
		if( !checkSequence(sequence, type) )
			throw new InvalidSequenceException();
		mGuessedAccession = header.split("[ \t]")[0];
	}
	
	public static boolean checkSequence( String sequence, SequenceType type ) {
		if( sequence.isEmpty() )
			return false;
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
				return false;
		return true;
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
	}	
}
