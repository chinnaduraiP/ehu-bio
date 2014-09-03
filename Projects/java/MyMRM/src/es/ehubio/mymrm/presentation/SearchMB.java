package es.ehubio.mymrm.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import es.ehubio.mymrm.data.Peptide;

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
		for( DetailsBean experiment : bean.getExperiments() ) {
			if( experiment.getFragments().isEmpty() )			
				experiment.getFragments().addAll(db.getFragments(experiment.getPrecursor().getId()));
			if( experiment.getScores().isEmpty() )
				experiment.getScores().addAll(db.getScores(experiment.getEvidence().getId()));
		}
		return "transitions";
	}
}
