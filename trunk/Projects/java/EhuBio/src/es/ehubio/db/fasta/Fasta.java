package es.ehubio.db.fasta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import es.ehubio.db.Aminoacid;
import es.ehubio.db.Nucleotide;
import es.ehubio.io.UnixCfgReader;

public final class Fasta {
	private final String sequence;
	private final String header;
	private final SequenceType type;
	private final String entry;
	private final String accession;
	private final String description;
	private final String proteinName;
	private final String geneName;
	
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
		this(header, guessParser(header), sequence, type);
	}
	
	public Fasta( String header, HeaderParser parser, String sequence, SequenceType type ) throws InvalidSequenceException {
		assert header != null && sequence != null;
		this.header = header;
		this.sequence = sequence.trim().replaceAll("[ \t]", "");		
		this.type = type;
		checkSequence(this.sequence, type);
		this.entry = header.split("[ \t]")[0];
		if( parser == null ) {
			accession = null;
			description = null;
			proteinName = null;
			geneName = null;
		} else {
			accession = parser.getAccession();
			description = parser.getDescription();
			proteinName = parser.getProteinName();
			geneName = parser.getGeneName();
		}
	}
	
	private static HeaderParser guessParser( String header ) {
		HeaderParser parser = new UniprotParser();
		if( parser.parse(header) )
			return parser;
		parser = new NextprotParser();
		if( parser.parse(header) )
			return parser;
		return null;
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
	
	public String getSequence() {
		return sequence;
	}
	
	public String getHeader() {
		return header;
	}
	
	public SequenceType getType() {
		return type;
	}
	
	public String getEntry() {
		return entry;
	}
	
	public String getAccession() {
		return accession;
	}
	
	public String getProteinName() {
		return proteinName;
	}
	
	public String getGeneName() {
		return geneName;
	}
	
	public static List<Fasta> readEntries( String path, SequenceType type ) throws IOException, InvalidSequenceException {
		Reader rd;
		if( path.endsWith("gz") )
			rd = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
		else
			rd = new FileReader(path);
		List<Fasta> list = readEntries(rd, type);
		rd.close();
		return list;
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
	
	public static void writeEntries( String path, Iterable<Fasta> list) throws FileNotFoundException, IOException {
		Writer wr;
		if( path.endsWith("gz") )
			wr = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path)));
		else
			wr = new FileWriter(path);
		writeEntries(wr, list);
		wr.close();
	}
	
	public static void writeEntries( Writer wr, Iterable<Fasta> list) {
		PrintWriter pw = new PrintWriter(wr);
		for( Fasta f : list ) {
			pw.println(">" + f.getHeader());
			pw.println(f.getSequence());
		}
		pw.flush();
	}

	public String getDescription() {
		return description;
	}
}
