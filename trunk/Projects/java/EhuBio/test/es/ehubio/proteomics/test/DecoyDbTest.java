package es.ehubio.proteomics.test;

import static org.junit.Assert.*;

import org.junit.Test;

import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.pipeline.DecoyDb;

public class DecoyDbTest {
	@Test
	public void test() {		
		String target = "ACDEFGKHILMRSTGGVWKLLGWS";
		String ok = "GFEDCAKMLIHRWVGGTSKSWGLL";
		String decoy = DecoyDb.getDecoy(target, DecoyDb.Strategy.PSEUDO_REVERSE, Enzyme.TRYPSIN);
		assertEquals(ok, decoy);
		
		target = "ACDEFGKHILMRSTGGVWKLLGWSK";
		ok = "GFEDCAKMLIHRWVGGTSKSWGLLK";
		decoy = DecoyDb.getDecoy(target, DecoyDb.Strategy.PSEUDO_REVERSE, Enzyme.TRYPSIN);
		assertEquals(ok, decoy);
	}

}
