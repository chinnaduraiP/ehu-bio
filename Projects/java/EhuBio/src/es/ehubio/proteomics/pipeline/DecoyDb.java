package es.ehubio.proteomics.pipeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.io.Streams;
import es.ehubio.proteomics.Enzyme;

public class DecoyDb {
	public static enum Strategy { REVERSE, PSEUDO_REVERSE }
	
	public static void create( String targetPath, String decoyPath, Strategy strategy, Enzyme enzyme, String decoyPrefix) throws FileNotFoundException, IOException, InvalidSequenceException {
		Reader targetReader = Streams.getTextReader(targetPath); 
		Writer decoyWriter = Streams.getTextWriter(decoyPath);
		create(targetReader,decoyWriter,strategy,enzyme,decoyPrefix);
		targetReader.close();
		decoyWriter.close();
	}

	public static void create(Reader targetReader, Writer decoyWriter, Strategy strategy, Enzyme enzyme, String decoyPrefix) throws IOException, InvalidSequenceException {
		List<Fasta> targets = Fasta.readEntries(targetReader, SequenceType.PROTEIN);
		List<Fasta> decoys = new ArrayList<>();
		
		for( Fasta target : targets )
			decoys.add(getDecoy(target, strategy, enzyme, decoyPrefix));
		Fasta.writeEntries(decoyWriter, decoys);
	}
	
	public static Fasta getDecoy(Fasta target, Strategy strategy, Enzyme enzyme, String decoyPrefix) {
		String seq = getDecoy(target.getSequence(), strategy, enzyme);
		String desc = String.format("Decoy for %s using %s strategy", target.getAccession(), strategy);
		if( enzyme != null )
			desc = String.format("%s with %s", desc, enzyme);
		return new Fasta(decoyPrefix+target.getAccession(), desc, seq, SequenceType.PROTEIN);
	}

	public static String getDecoy(String sequence, Strategy strategy, Enzyme enzyme) {
		if( sequence == null || sequence.isEmpty() )
			return sequence;
		if( strategy != Strategy.PSEUDO_REVERSE )
			throw new UnsupportedOperationException(String.format("Decoy strategy %s not supported", strategy));
		
		String[] peptides = Digester.digestSequence(sequence, enzyme);
		StringBuilder decoy = new StringBuilder();
		for( int p = 0; p < peptides.length - 1; p++ )
			decoy.append(pseudoReverse(peptides[p]));
		
		String lastPeptide = peptides[peptides.length-1];
		String test = lastPeptide+"AAA";
		if( Digester.digestSequence(test, enzyme).length > 1 )
			decoy.append(pseudoReverse(lastPeptide));
		else
			decoy.append(reverse(lastPeptide));
		
		return decoy.toString();
	}
	
	private static String reverse( String seq ) {
		StringBuilder rev = new StringBuilder();
		char[] chars = seq.toCharArray();
		int last = chars.length-1;
		for( int i = 0; i <= last; i++ )
			rev.append(chars[last-i]);
		return rev.toString();
	}
	
	private static String pseudoReverse( String seq ) {
		int last = seq.length()-1;
		return reverse(seq.substring(0, last))+seq.charAt(last);
	}
}