package es.ehubio.proteomics.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.pipeline.Digester;

public class Digestion {
	private static final Logger logger = Logger.getLogger(Digestion.class.getName());
	
	@Test
	public void testTrypsine() {
		String seq = "ASDFKFDSARQWERPQWE";
		String[] list = Digester.digestSequence(seq, Enzyme.TRYPSIN);
		/*logger.info("Sequence: "+seq);
		for( String pep : list )
			logger.info("Peptide: "+pep);*/
		assertEquals(3, list.length);
		assertTrue(list[2].equalsIgnoreCase("QWERPQWE"));
	}

	@Test
	public void testTrypsineP() {
		String seq = "ASDFKFDSARQWERPQWE";
		String[] list = Digester.digestSequence(seq, Enzyme.TRYPSIN_P);
		/*logger.info("Sequence: "+seq);
		for( String pep : list )
			logger.info("Peptide: "+pep);*/
		assertEquals(4, list.length);
		assertTrue(list[3].equalsIgnoreCase("PQWE"));
	}
	
	@Test
	public void testProtein() {
		String seq = "MGKVKVGVNGFGRIGRLVTRAAFNSGKVDIVAINDPFIDLNYMVYMFQYDSTHGKFHGTVKAENGKLVINGNPITIFQERDPSKIKWGDAGAEYVVESTGVFTTMEKAGAHLQGGAKRVIISAPSADAPMFVMGVNHEKYDNSLKIISNASCTTNCLAPLAKVIHDNFGIVEGLMTTVHAITATQKTVDGPSGKLWRDGRGALQNIIPASTGAAKAVGKVIPELNGKLTGMAFRVPTANVSVVDLTCRLEKPAKYDDIKKVVKQASEGPLKGILGYTEHQVVSSDFNSDTHSSTFDAGAGIALNDHFVKLISWYDNEFGYSNRVVDLMAHMASKE";
		List<String> list = Digester.digestSequence(seq, Enzyme.TRYPSIN, 2);
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int dif = o2.length()-o1.length();
				if( dif != 0 )
					return dif;
				return o1.compareTo(o2);
			}
		});
		for( String pep : list )
			logger.info("Peptide: "+pep);
		assertEquals(105, list.size());
	}
}
