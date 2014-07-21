package es.ehubio.proteomics;

import es.ehubio.model.ProteinModification;

/**
 * Post-Translational Mofidication in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class Ptm extends ProteinModification {	
	private Double massDelta;
		
	public Double getMassDelta() {
		return massDelta;
	}

	public void setMassDelta(Double massDelta) {
		this.massDelta = massDelta;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(getName());
		if( getResidues() != null )
			str.append("+"+getResidues());
		if( getPosition() != null )
			str.append(String.format("(%d)", getPosition()));
		return str.toString();
	}
}
