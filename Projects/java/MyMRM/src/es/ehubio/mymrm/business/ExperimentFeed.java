package es.ehubio.mymrm.business;

import es.ehubio.mymrm.data.Experiment;
import es.ehubio.proteomics.Peptide;
import es.ehubio.tools.PAnalyzerCli;

public class ExperimentFeed {
	private final Experiment experiment;
	private final PAnalyzerCli.Configuration configuration;
	private final Peptide.Confidence confidence;
	private String status;
	
	public ExperimentFeed( Experiment experiment, PAnalyzerCli.Configuration configuration, Peptide.Confidence confidence ) {
		this.experiment = experiment;
		this.configuration = configuration;
		this.confidence = confidence;
	}

	public PAnalyzerCli.Configuration getConfiguration() {
		return configuration;
	}

	public Peptide.Confidence getConfidence() {
		return confidence;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
