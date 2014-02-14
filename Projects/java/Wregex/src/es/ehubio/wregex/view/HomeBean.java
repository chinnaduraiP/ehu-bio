package es.ehubio.wregex.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class HomeBean implements Serializable {
	private static final long serialVersionUID = 1L;
	final List<PageSummary> pages;
	final List<LatestNew> news;	

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
		
		news = new ArrayList<>();
		news.add(new LatestNew("Feb 13, 2014", "Now it is possible to search for all Wregex and ELM motifs at the same time!"));
		news.add(new LatestNew("Feb 11, 2014", "Included proteins of cancer genes from nature12912 paper as a predefined target"));
		news.add(new LatestNew("Feb 11, 2014", "Wregex-v1.1 beta features moved to a test server"));
		news.add(new LatestNew("Feb 11, 2014", "Included beta support for COSMIC database! Results are sorted by mutations when COSMIC is enabled"));
		news.add(new LatestNew("Feb 10, 2014", "Included human proteome as a predefined target"));
		news.add(new LatestNew("Jan 06, 2014", "Wregex v1.0 published in Bioinformatics"));
	}
	
	public List<PageSummary> getPages() {
		return pages;
	}
	
	public List<LatestNew> getNews() {
		return news;
	}
	
	public String getSignature() {
		return "Wregex (v1.1b2)";
	}
	
	public String getLastUpdated() {
		return news.get(0).getDate();
	}
}