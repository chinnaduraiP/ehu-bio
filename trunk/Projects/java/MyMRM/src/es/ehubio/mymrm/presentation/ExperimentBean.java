package es.ehubio.mymrm.presentation;

import java.util.ArrayList;
import java.util.List;

import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.Fragment;

public class ExperimentBean {
	private Experiment entity;
	private final List<Fragment> fragments = new ArrayList<>();

	public List<Fragment> getFragments() {
		return fragments;
	}

	public Experiment getEntity() {
		return entity;
	}

	public void setEntity(Experiment entity) {
		this.entity = entity;
	}
}
