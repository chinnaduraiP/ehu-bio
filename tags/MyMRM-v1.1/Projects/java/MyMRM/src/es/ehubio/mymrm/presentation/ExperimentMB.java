package es.ehubio.mymrm.presentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
import es.ehubio.panalyzer.Configuration;
import es.ehubio.proteomics.Peptide;

@ManagedBean
@SessionScoped
public class ExperimentMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Experiment entity = new Experiment();
	private String instrument;
	private String ionization;
	private String fragmentation;
	private String chromatography;
	private final Set<String> files = new HashSet<>();
	private Peptide.Confidence peptideConfidence = Peptide.Confidence.DISCRIMINATING;
	private final Configuration cfg;
	
	public ExperimentMB() {
		cfg = new Configuration();
		cfg.initialize();
	}

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

		Configuration cfg = new Configuration();
		cfg.setDescription(getEntity().getName());
		cfg.setFilterDecoys(true);
		cfg.setInputs(new HashSet<String>());
		for( String file : files )
			cfg.getInputs().add(new File(getTmpDir(),file).getAbsolutePath());

		ExperimentFeed feed = new ExperimentFeed(experiment, cfg, peptideConfidence);
		try {
			Database.feed(feed);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("experimentMB");
	}
	
	public static String getTmpDir() {
		//return FacesContext.getCurrentInstance().getExternalContext().getInitParameter("MyMRM.fastaDir");
		return System.getProperty("java.io.tmpdir");
	}
	
	public String getFiles() {
		return CsvUtils.getCsv(';', files.toArray());
	}
	
	public boolean isReady() {
		return !files.isEmpty() && entity.getName() != null && !entity.getName().isEmpty(); 
	}

	public Peptide.Confidence getPeptideConfidence() {
		return peptideConfidence;
	}

	public void setPeptideConfidence(Peptide.Confidence peptideConfidence) {
		this.peptideConfidence = peptideConfidence;
	}

	public Configuration getCfg() {
		return cfg;
	}	
}
