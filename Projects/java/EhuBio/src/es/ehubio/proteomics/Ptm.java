package es.ehubio.proteomics;

/**
 * Post-Translational Mofidication in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class Ptm {	
	private String aminoacid;
	private Integer position;
	private String name;
	
	public String getAminoacid() {
		return aminoacid;
	}
	
	public void setAminoacid(String aminoacid) {
		this.aminoacid = aminoacid;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(name);
		if( aminoacid != null )
			str.append("+"+aminoacid);
		if( position != null )
			str.append(String.format("(%d)", position));
		return str.toString();
	}
}
