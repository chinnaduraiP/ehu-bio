package es.ehubio.mymrm.presentation;

import java.util.ArrayList;
import java.util.List;

public class PrecursorBean {
	private double mz;
	private String charge;
	private List<ExperimentBean> experiments = new ArrayList<>();

	public double getMz() {
		return mz;
	}
	
	public void setMz(double mz) {
		this.mz = mz;
	}

	public List<ExperimentBean> getExperiments() {
		return experiments;
	}

	public String getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = String.format("%s%c", Math.abs(charge), charge>=0?'+':'-');
	}
}
