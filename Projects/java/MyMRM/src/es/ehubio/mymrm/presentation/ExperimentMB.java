package es.ehubio.mymrm.presentation;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import es.ehubio.mymrm.data.Experiment;

@ManagedBean
@RequestScoped
public class ExperimentMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Experiment entity = new Experiment();
	private String instrument;
	private String chromatography;
	private String pax; 
	private String id;
	
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

	public String getPax() {
		return pax;
	}

	public void setPax(String pax) {
		this.pax = pax;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
