package es.ehu.grk.wregex.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import es.ehu.grk.wregex.Wregex;

@ManagedBean
@RequestScoped
public class HomeBean implements Serializable {
	private static final long serialVersionUID = 1L;
	final List<PageSummary> pages;		

	public HomeBean() {
		pages = new ArrayList<>();
		
		PageSummary page = new PageSummary();
		page.setName("Search");
		page.setDescription(
			"Search motifs in protein sequences provided as an input fasta file. " +
			"The motif can be selected from a dropdown list or a custom motif can be provided by the user by " +
			"entering a regular expression and an optional PSSM. This PSSM can be builded using the Training page." );
		page.setAction("search");
		pages.add(page);
		
		page = new PageSummary();
		page.setName("Training");
		page.setDescription(
			"Build a custom PSSM by providing a regular expression and a set of training motifs. " +
			"Please read first the user manual to get familiar with matching groups in Wregex regular expressions.");
		page.setAction("training");
		pages.add(page);
		
		page = new PageSummary();
		page.setName("Documentation");
		page.setDescription(
			"Here there is the user manual and a paper explaining the details of the Wregex algorithm." );
		page.setAction("documentation");
		pages.add(page);
		
		page = new PageSummary();
		page.setName("Downloads");
		page.setDescription(
			"Wregex is free software and licensed under the GPL. In this page you can find a link to the source code " +
			"and the binary redistributable." );
		page.setAction("downloads");
		pages.add(page);		
	}
	
	public List<PageSummary> getPages() {
		return pages;
	}
	
	public String getSignature() {
		return "Wregex (v"+Wregex.getVersion()+")";
	}
}