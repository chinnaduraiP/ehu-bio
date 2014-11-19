package es.ehubio.proteomics.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.io.MsMsFile;

public class MsfTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		MsMsData data = MsMsFile.autoLoad("/home/gorka/Bio/Proyectos/Proteómica/spHPP/Work/Sequest/ProteomeDiscoverer/PD14.msf");
		assertEquals(data.getProteins().size(), 623);
	}

}
