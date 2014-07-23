package es.ehubio.mymrm.data;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the Peptide database table.
 * 
 */
@Entity
@NamedQuery(name="Peptide.findAll", query="SELECT p FROM Peptide p")
public class Peptide implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@Lob
	private String massSequence;

	@Lob
	private String sequence;

	//bi-directional many-to-one association to Precursor
	@OneToMany(mappedBy="peptideBean")
	private List<Precursor> precursors;

	public Peptide() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMassSequence() {
		return this.massSequence;
	}

	public void setMassSequence(String massSequence) {
		this.massSequence = massSequence;
	}

	public String getSequence() {
		return this.sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public List<Precursor> getPrecursors() {
		return this.precursors;
	}

	public void setPrecursors(List<Precursor> precursors) {
		this.precursors = precursors;
	}

	public Precursor addPrecursor(Precursor precursor) {
		getPrecursors().add(precursor);
		precursor.setPeptideBean(this);

		return precursor;
	}

	public Precursor removePrecursor(Precursor precursor) {
		getPrecursors().remove(precursor);
		precursor.setPeptideBean(null);

		return precursor;
	}

}