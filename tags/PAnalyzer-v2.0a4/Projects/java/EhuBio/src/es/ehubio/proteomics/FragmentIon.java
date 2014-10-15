package es.ehubio.proteomics;

import java.io.Serializable;

public class FragmentIon implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public double getMzExp() {
		return mzExp;
	}
	
	public void setMzExp(double mzExp) {
		this.mzExp = mzExp;
	}
	
	public double getMzCalc() {
		return getMzExp()-getMzError();
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getMzError() {
		return mzError;
	}

	public void setMzError(double mzError) {
		this.mzError = mzError;
	}
	
	public IonType getType() {
		return type;
	}

	public void setType(IonType type) {
		this.type = type;
	}

	private double mzExp;
	private double intensity;
	private int charge;
	private int index;
	private double mzError;
	private IonType type;
}
