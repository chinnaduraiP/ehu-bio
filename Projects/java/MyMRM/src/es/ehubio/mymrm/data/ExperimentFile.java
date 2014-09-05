package es.ehubio.mymrm.data;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the ExperimentFile database table.
 * 
 */
@Entity
@NamedQuery(name="ExperimentFile.findAll", query="SELECT e FROM ExperimentFile e")
public class ExperimentFile implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	private String identification;

	private String spectra;

	//bi-directional many-to-one association to Experiment
	@ManyToOne
	@JoinColumn(name="experiment")
	private Experiment experimentBean;

	public ExperimentFile() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdentification() {
		return this.identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getSpectra() {
		return this.spectra;
	}

	public void setSpectra(String spectra) {
		this.spectra = spectra;
	}

	public Experiment getExperimentBean() {
		return this.experimentBean;
	}

	public void setExperimentBean(Experiment experimentBean) {
		this.experimentBean = experimentBean;
	}

}