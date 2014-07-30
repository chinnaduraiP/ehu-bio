package es.ehubio.mymrm.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import es.ehubio.mymrm.data.LatestNew;
import es.ehubio.mymrm.data.PageSummary;

@ManagedBean
@RequestScoped
public class HomeBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private final List<PageSummary> pages;
	private List<PageSummary> firstPages = new ArrayList<>();
	private PageSummary lastPage = null;
	private final List<LatestNew> news;	

	public HomeBean() {
		pages = new ArrayList<>();
		
		PageSummary page = new PageSummary();
		page.setName("Home");
		page.setDescription("The MyMRM home page.");
		page.setAction("home");
		addPage(page);
		
		page = new PageSummary();
		page.setName("Upload");
		page.setDescription("Feed database with new experiments.");
		page.setAction("feed");
		addPage(page);
		
		page = new PageSummary();
		page.setName("Materials & Methods");
		page.setDescription("Materials and methods.");
		page.setAction("methods");
		addPage(page);
		
		page = new PageSummary();
		page.setName("Search Peptide");
		page.setDescription("Search database for evidences and transactions of a given peptide secuence.");
		page.setAction("search");
		addPage(page);
		
		page = new PageSummary();
		page.setName("Search Protein");
		page.setDescription("Search database for evidences and transactions of unique peptides for a given protein.");
		page.setAction("search");
		addPage(page);
		
		news = new ArrayList<>();
		news.add(new LatestNew("Jul 23, 2014", "Started MyMRM web GUI"));
	}
	
	public List<PageSummary> getPages() {
		return pages;
	}
	
	public List<PageSummary> getFirstPages() {
		return firstPages;
	}
	
	public PageSummary getLastPage() {
		return lastPage;
	}
	
	public void addPage( PageSummary page ) {
		if( lastPage != null )
			firstPages.add(lastPage);
		lastPage = page;
		pages.add(page);
	}
	
	public List<LatestNew> getNews() {
		return news;
	}
	
	public String getSignature() {
		return "MyMRM (v0.1)";
	}
	
	public String getLastUpdated() {
		return news.get(0).getDate();
	}
}