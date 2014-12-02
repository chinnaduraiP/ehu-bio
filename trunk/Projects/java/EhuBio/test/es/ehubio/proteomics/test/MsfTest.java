package es.ehubio.proteomics.test;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.pipeline.Filter;

public class MsfTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		MsMsData data = MsMsFile.autoLoad("/home/gorka/Bio/Proyectos/Proteómica/spHPP/Work/Sequest/ProteomeDiscoverer/PD14.msf",false);
		Filter filter = new Filter(data);
		filter.setPeptideScoreThreshold(new Score(ScoreType.PEPTIDE_MSF_CONFIDENCE, 2.5));
		filter.run();		
		assertEquals(data.getProteins().size(), 22);
	}
}
