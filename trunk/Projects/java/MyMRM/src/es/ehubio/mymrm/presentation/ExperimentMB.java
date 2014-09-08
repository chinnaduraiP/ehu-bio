package es.ehubio.mymrm.presentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehubio.io.CsvUtils;
import es.ehubio.mymrm.business.Database;
import es.ehubio.mymrm.business.ExperimentFeed;
import es.ehubio.mymrm.data.Chromatography;
import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.FragmentationType;
import es.ehubio.mymrm.data.Instrument;
import es.ehubio.mymrm.data.IonizationType;
import es.ehubio.proteomics.Peptide;
import es.ehubio.tools.PAnalyzerCli;

@ManagedBean
@SessionScoped
public class ExperimentMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Experiment entity = new Experiment();
	private String instrument;
	private String ionization;
	private String fragmentation;
	private String chromatography;
	private String decoyRegex = "decoy";
	private String psmScore;
	private final Set<String> files = new HashSet<>();	

	public Experiment getEntity() {
		return entity;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public String getChromatography() {
		return chromatography;
	}

	public void setChromatography(String chromatography) {
		this.chromatography = chromatography;
	}

	public String getIonization() {
		return ionization;
	}

	public void setIonization(String ionization) {
		this.ionization = ionization;
	}

	public String getFragmentation() {
		return fragmentation;
	}

	public void setFragmentation(String fragmentation) {
		this.fragmentation = fragmentation;
	}
	
	public void uploadFile( FileUploadEvent event ) {
		try {
			UploadedFile file = event.getFile();
			InputStream is = file.getInputstream();
			OutputStream os = new FileOutputStream(new File(getTmpDir(), file.getFileName()));
			IOUtils.copy(is, os);
			is.close();
			os.close();
			files.add(file.getFileName());
		} catch( Exception e ) {			
		}
	}
	
	public void feed() {
		if( !isReady() )
			return;
		
		Experiment experiment = getEntity();
		experiment.setInstrumentBean(Database.findById(Instrument.class, Integer.parseInt(getInstrument())));
		experiment.setIonizationTypeBean(Database.findById(IonizationType.class, Integer.parseInt(getIonization())));
		experiment.setFragmentationTypeBean(Database.findById(FragmentationType.class, Integer.parseInt(getFragmentation())));
		experiment.setChromatographyBean(Database.findById(Chromatography.class, Integer.parseInt(getChromatography())));

		PAnalyzerCli.Configuration cfg = new PAnalyzerCli.Configuration();
		cfg.description = getEntity().getName();
		cfg.operation = "grp";
		cfg.psmScore = psmScore;
		cfg.peptideFdr = 0.01;
		cfg.groupFdr = 0.01;
		cfg.inputs = new ArrayList<>();
		for( String file : files ) {
			if( !file.contains("mzid") )
				continue;
			PAnalyzerCli.Configuration.InputFile input = new PAnalyzerCli.Configuration.InputFile();
			input.path = new File(getTmpDir(),file).getAbsolutePath();
			input.ions = getTmpDir();
			input.decoyRegex = decoyRegex;
			cfg.inputs.add(input);
		}

		ExperimentFeed feed = new ExperimentFeed(experiment, cfg, Peptide.Confidence.DISCRIMINATING);
		try {
			Database.feed(feed);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("experimentMB");
	}
	
	public static String getTmpDir() {
		return FacesContext.getCurrentInstance().getExternalContext().getInitParameter("MyMRM.fastaDir");
	}

	public String getDecoyRegex() {
		return decoyRegex;
	}

	public void setDecoyRegex(String decoyRegex) {
		this.decoyRegex = decoyRegex;
	}

	public String getPsmScore() {
		return psmScore;
	}

	public void setPsmScore(String psmScore) {
		this.psmScore = psmScore;
	}
	
	public String getFiles() {
		return CsvUtils.getCsv(';', files.toArray());
	}
	
	public boolean isReady() {
		return !files.isEmpty() && entity.getName() != null && !entity.getName().isEmpty(); 
	}
}
