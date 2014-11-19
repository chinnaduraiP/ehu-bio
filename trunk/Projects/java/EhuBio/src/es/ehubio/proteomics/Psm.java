package es.ehubio.proteomics;

import java.util.ArrayList;
import java.util.List;

/**
 * Peptide-Spectrum Match in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class Psm extends DecoyBase {
	private static int idCount = 1;
	private final int id;
	private int charge;
	private Double calcMz;
	private double expMz;
	private Integer rank;
	private Spectrum spectrum;
	private Peptide peptide;
	private List<FragmentIon> ions = new ArrayList<>();
	
	public Psm() {
		id = idCount++;
	}
	
	public int getId() {
		return id;
	}
	
	public int getCharge() {
		return charge;
	}
	
	public void setCharge(int charge) {
		this.charge = charge;
	}
	
	public Double getCalcMz() {
		return calcMz;
	}

	public void setCalcMz(Double calcMz) {
		this.calcMz = calcMz;
	}

	public double getExpMz() {
		return expMz;
	}

	public void setExpMz(double expMz) {
		this.expMz = expMz;
	}
	
	public Double getPpm() {
		if( getCalcMz() == null )
			return null;
		return Math.abs((getExpMz()-getCalcMz())/getCalcMz()*1000000);
	}
	
	public Integer getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public Spectrum getSpectrum() {
		return spectrum;
	}
	
	public void linkSpectrum(Spectrum spectrum) {
		/*if( this.spectrum != null )
			this.spectrum.getPsms().remove(this);*/
		this.spectrum = spectrum;
		if( spectrum != null )
			this.spectrum.addPsm(this);
	}
	
	public Peptide getPeptide() {
		return peptide;
	}

	public void linkPeptide(Peptide peptide) {
		/*if( this.peptide != null )
			this.peptide.getPsms().remove(this);*/
		this.peptide = peptide;
		if( peptide != null )
			peptide.addPsm(this);
	}
	
	public void guessIons() {
		if( !getIons().isEmpty() )
			return;
		
	}
	
	@Override
	public Boolean getDecoy() {
		if( peptide == null )
			return null;
		return peptide.getDecoy();
	}
	
	@Override
	public void setDecoy(Boolean decoy) {
		if( peptide != null )
			peptide.setDecoy(decoy);
	}
	
	@Override
	public String toString() {
		return ""+getId();
	}
	
	@Override
	protected String buildUniqueString() {
		return toString();
	}

	public List<FragmentIon> getIons() {
		return ions;
	}

	public void setIons(List<FragmentIon> ions) {
		this.ions = ions;
	}
}