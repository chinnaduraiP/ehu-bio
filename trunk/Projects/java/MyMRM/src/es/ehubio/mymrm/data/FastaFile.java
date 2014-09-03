package es.ehubio.mymrm.data;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the FastaFile database table.
 * 
 */
@Entity
@NamedQuery(name="FastaFile.findAll", query="SELECT f FROM FastaFile f")
public class FastaFile implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;

	@Lob
	private String description;

	private String name;

	public FastaFile() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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

}