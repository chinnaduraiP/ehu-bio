package es.ehubio.mymrm.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.Precursor;

@ManagedBean
@SessionScoped
public class SearchMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private String peptideSequence;
	private List<PeptideBean> peptides;
	private PrecursorBean precursor;
	
	public String getPeptideSequence() {
		return peptideSequence;
	}
	
	public void setPeptideSequence(String peptide) {
		this.peptideSequence = peptide;
	}
	
	public void search( DatabaseMB db ) {
		peptides = new ArrayList<>();
		for( Peptide peptide : db.search(peptideSequence) ) {
			PeptideBean bean = new PeptideBean();
			bean.setEntity(peptide);
			peptides.add(bean);
		}
	}

	public List<PeptideBean> getPeptides() {
		return peptides;
	}

	public PrecursorBean getPrecursor() {
		return precursor;
	}
	
	public String showDetails( PrecursorBean bean, DatabaseMB db ) {
		this.precursor = bean;
		for( ExperimentBean experiment : bean.getExperiments() ) {
			experiment.getFragments().clear();
			List<Precursor> precursors = db.getPrecursors(bean.getMz(), experiment.getEntity().getId());
			for( Precursor precursor : precursors )
				experiment.getFragments().addAll(db.getFragments(precursor.getId()));
		}
		return "transitions";
	}
}
