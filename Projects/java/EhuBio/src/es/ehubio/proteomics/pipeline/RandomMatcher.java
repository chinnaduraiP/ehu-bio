package es.ehubio.proteomics.pipeline;

import es.ehubio.proteomics.Protein;

public interface RandomMatcher {
	double getExpected( Protein protein );
}
