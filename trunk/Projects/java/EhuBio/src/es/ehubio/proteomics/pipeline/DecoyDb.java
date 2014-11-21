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
		
		for( Fasta target : targets ) {
			String seq = getDecoy(target.getSequence(),strategy,enzyme);
			Fasta decoy = new Fasta(decoyPrefix+target.getAccession(), "", seq, SequenceType.PROTEIN);
			decoys.add(decoy);
		}
		Fasta.writeEntries(decoyWriter, decoys);
	}

	public static String getDecoy(String sequence, Strategy strategy, Enzyme enzyme) {
		if( strategy != Strategy.PSEUDO_REVERSE )
			throw new UnsupportedOperationException(String.format("Decoy strategy %s not supported", strategy));
		String[] peptides = Digester.digestSequence(sequence, enzyme);
		StringBuilder decoy = new StringBuilder();
		for( int p = 0; p < peptides.length; p++ ) {
			String peptide = peptides[p];
			int last = peptide.length();
			if( p < peptides.length-1 )
				last--;
			char[] chars = peptide.toCharArray();
			for( int i=last-1; i>=0; i-- )
				decoy.append(chars[i]);
			if( last < peptide.length() )
				decoy.append(chars[last]);
		}
		return decoy.toString();
	}
}