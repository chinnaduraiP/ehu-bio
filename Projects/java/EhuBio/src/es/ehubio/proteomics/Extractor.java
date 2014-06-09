package es.ehubio.proteomics;

import es.ehubio.proteomics.Protein.Confidence;

/**
 * Class for extracting information about a proteomics experiment.
 * 
 * @author gorka
 *
 */
public final class Extractor {
	private MsMsData data;	
	private boolean countDecoy = false;
	
	public void setData( MsMsData data ) {
		this.data = data;
	}
	
	public boolean isCountDecoy() {
		return countDecoy;
	}

	public void setCountDecoy(boolean countDecoy) {
		this.countDecoy = countDecoy;
	}
	
	public double getPsmFdr() {
		int decoy = 0;
		int target = 0;
		for( Psm psm : data.getPsms() )
			if( Boolean.TRUE.equals(psm.getDecoy()) )
				decoy++;
			else
				target++;
		return getFdr(decoy, target);
	}
	
	public double getPeptideFdr() {
		int decoy = 0;
		int target = 0;
		for( Peptide peptide : data.getPeptides() )
			if( Boolean.TRUE.equals(peptide.getDecoy()) )
				decoy++;
			else
				target++;
		return getFdr(decoy, target);
	}
	
	public double getProteinFdr() {
		int decoy = 0;
		int target = 0;
		for( Protein protein : data.getProteins() )
			if( Boolean.TRUE.equals(protein.getDecoy()) )
				decoy++;			
			else
				target++;
		return getFdr(decoy, target);
	}
	
	public double getGroupFdr() {
		int decoy = 0;
		int target = 0;
		for( ProteinGroup group : data.getGroups() ) {
			if( group.getConfidence() == Confidence.NON_CONCLUSIVE )
				continue;
			if( Boolean.TRUE.equals(group.getDecoy()) )
				decoy++;
			else
				target++;
		}
		return getFdr(decoy, target);	
	}
	
	private double getFdr( int decoy, int target ) {
		if( target == 0 )
			return 0.0;
		if( isCountDecoy() )
			return (2.0*decoy)/(target+decoy);
		return ((double)decoy)/target;
	}
}