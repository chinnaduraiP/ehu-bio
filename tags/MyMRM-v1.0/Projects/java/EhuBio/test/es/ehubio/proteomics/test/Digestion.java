package es.ehubio.proteomics.test;

import static org.junit.Assert.*;

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
		logger.info("Sequence: "+seq);
		for( String pep : list )
			logger.info("Peptide: "+pep);
		assertEquals(3, list.length);
		assertTrue(list[2].equalsIgnoreCase("QWERPQWE"));
	}

	@Test
	public void testTrypsineP() {
		String seq = "ASDFKFDSARQWERPQWE";
		String[] list = Digester.digestSequence(seq, Enzyme.TRYPSIN_P);
		logger.info("Sequence: "+seq);
		for( String pep : list )
			logger.info("Peptide: "+pep);
		assertEquals(4, list.length);
		assertTrue(list[3].equalsIgnoreCase("PQWE"));
	}
}