package es.ehubio.proteomics.pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class Digester {
	public static String[] digestSequence( String sequence, Enzyme enzyme ) {
		return enzyme.getPattern().split(sequence);
		/*List<String> list = new ArrayList<>();
		Pattern pattern = enzyme.getPattern();
		Matcher matcher = pattern.matcher(sequence);
		int start = 0;
		while( start < sequence.length() && matcher.find(start) ) {
			list.add(sequence.substring(start, matcher.start()+1));
			start = matcher.start()+1;
		}
		if( start < sequence.length() )
			list.add(sequence.substring(start, sequence.length()));
		return list;*/
	}
	
	public static Set<Peptide> digestDatabase( String path, Enzyme enzyme, int minLength ) throws IOException, InvalidSequenceException {
		List<Fasta> database = Fasta.readEntries(path, SequenceType.PROTEIN);
		Map<String,Peptide> peptides = new HashMap<>();
		for( Fasta fasta : database ) {
			Protein protein = new Protein();
			protein.setFasta(fasta);
			String[] seqs = digestSequence(protein.getSequence(), enzyme);
			for( String seq : seqs ) {
				if( seq.length() < minLength )
					continue;
				Peptide peptide = peptides.get(seq);
				if( peptide == null ) {
					peptide = new Peptide();
					peptide.setSequence(seq);
					peptides.put(seq, peptide);
				}
				peptide.addProtein(protein);
			}
		}
		return new HashSet<>(peptides.values());
	}
}
