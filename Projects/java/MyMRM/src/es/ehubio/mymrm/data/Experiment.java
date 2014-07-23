package es.ehubio.mymrm.data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the Experiment database table.
 * 
 */
@Entity
@NamedQuery(name="Experiment.findAll", query="SELECT e FROM Experiment e")
public class Experiment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@Lob
	private String contact;

	@Lob
	private String description;

	private String name;

	//bi-directional many-to-one association to Chromatography
	@ManyToOne
	@JoinColumn(name="chromatography")
	private Chromatography chromatographyBean;

	//bi-directional many-to-one association to Instrument
	@ManyToOne
	@JoinColumn(name="instrument")
	private Instrument instrumentBean;

	//bi-directional many-to-one association to Transition
	@OneToMany(mappedBy="experimentBean")
	private List<Transition> transitions;

	public Experiment() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Chromatography getChromatographyBean() {
		return this.chromatographyBean;
	}

	public void setChromatographyBean(Chromatography chromatographyBean) {
		this.chromatographyBean = chromatographyBean;
	}

	public Instrument getInstrumentBean() {
		return this.instrumentBean;
	}

	public void setInstrumentBean(Instrument instrumentBean) {
		this.instrumentBean = instrumentBean;
	}

	public List<Transition> getTransitions() {
		return this.transitions;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public Transition addTransition(Transition transition) {
		getTransitions().add(transition);
		transition.setExperimentBean(this);

		return transition;
	}

	public Transition removeTransition(Transition transition) {
		getTransitions().remove(transition);
		transition.setExperimentBean(null);

		return transition;
	}

}