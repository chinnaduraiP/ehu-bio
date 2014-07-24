package es.ehubio.mymrm.presentation;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import es.ehubio.mymrm.data.Peptide;

@ManagedBean
@RequestScoped
public class SearchMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private String peptide;
	private List<Peptide> result;
	
	public String getPeptide() {
		return peptide;
	}
	
	public void setPeptide(String peptide) {
		this.peptide = peptide;
	}
	
	public void search( DatabaseMB db ) {
		result = db.search(peptide);
	}

	public List<Peptide> getResult() {
		return result;
	}
}
