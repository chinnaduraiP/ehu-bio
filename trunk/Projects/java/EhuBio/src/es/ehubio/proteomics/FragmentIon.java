package es.ehubio.proteomics;

public class FragmentIon {
	public FragmentIon() {		
	}
	
	public FragmentIon( double mz, double intensity ) {
		this.mz = mz;
		this.intensity = intensity;
	}
	
	public double getMz() {
		return mz;
	}
	
	public void setMz(double mz) {
		this.mz = mz;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	private double mz;
	private double intensity;
}
