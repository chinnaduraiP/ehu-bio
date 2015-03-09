package es.ehubio.proteomics.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;

public class Inferer {
	private final static Logger logger = Logger.getLogger(Inferer.class.getName());
	
	public static void relink(MsMsData data, String fastaPath, Digester.Config config) throws IOException, InvalidSequenceException{
		logger.info("Building peptide<->protein relations from fasta file ...");
		Map<String, List<Protein>> map = getMap(loadFasta(fastaPath), config);
		logger.info("Updating peptide<->protein relations in MS/MS Data ...");
		data.getProteins().clear();
		data.getGroups().clear();
		for( Peptide peptide : data.getPeptides() ) {
			//int prev = peptide.getProteins().size();
			peptide.getProteins().clear();
			for( Protein protein : map.get(peptide.getSequence().toLowerCase()) )
				protein.addPeptide(peptide);
			/*if( peptide.getProteins().size() != prev )
				logger.info(String.format("Peptide %s: %d->%d", peptide.getSequence(), prev, peptide.getProteins().size()));*/
			data.getProteins().addAll(peptide.getProteins());
		}
	}
	
	private static List<Protein> loadFasta( String fastaPath ) throws IOException, InvalidSequenceException {
		List<Protein> proteins = new ArrayList<>();
		List<Fasta> fastas = Fasta.readEntries(fastaPath, SequenceType.PROTEIN);
		for( Fasta fasta : fastas ) {
			Protein protein = new Protein();
			protein.setFasta(fasta);
			proteins.add(protein);
		}
		return proteins;
	}
	
	private static Map<String, List<Protein>> getMap( List<Protein> proteins, Digester.Config config ) {
		Map<String, List<Protein>> map = new HashMap<>();
		for( Protein protein : proteins )
			for( String pepSeq : Digester.digestSequence(protein.getSequence(), config) ) {
				List<Protein> list = map.get(pepSeq.toLowerCase());
				if( list == null ) {
					list = new ArrayList<>();
					map.put(pepSeq.toLowerCase(), list);
				}
				list.add(protein);
			}
		return map;
	}
}
