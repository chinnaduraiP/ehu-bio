package es.ehubio.wregex.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.wregex.Pssm;
import es.ehubio.wregex.Wregex;
import es.ehubio.wregex.model.BubbleChartData;
import es.ehubio.wregex.model.MotifDefinition;
import es.ehubio.wregex.model.MotifInformation;
import es.ehubio.wregex.model.ResultEx;
import es.ehubio.wregex.model.ResultGroupEx;
import es.ehubio.wregex.model.Services;

@ManagedBean
@ApplicationScoped
public class StatisticsBean {
	private final static Logger logger = Logger.getLogger(StatisticsBean.class.getName());
	private String jsonMotifs;
	@ManagedProperty(value="#{databasesBean}")
	private DatabasesBean databases;
	private boolean initialized = false;
	private final int topCount = 10;
	private final int maxMutations = 800;
	private final int minMutations = 4;
	private BubbleChartData motifs;
	private final List<String> displayTips;
	
	public StatisticsBean() {
		displayTips = new ArrayList<>();
		displayTips.add(String.format("Bubble size has been limited to %d mutations", maxMutations));
		displayTips.add(String.format("Motifs with less than %d mutations have been filtered", minMutations));
	}
	
	public DatabasesBean getDatabases() {
		return databases;
	}

	public void setDatabases(DatabasesBean databases) {
		this.databases = databases;
	}

	@PostConstruct
	public void init() {
		try {
			searchHumanProteome();
			createJson();
			initialized = true;
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void searchHumanProteome() throws IOException, InvalidSequenceException, Exception {		
		List<MotifInformation> allMotis = databases.getAllMotifs();
		motifs = new BubbleChartData("");
		BubbleChartData motif, child;
		List<ResultGroupEx> resultGroups;
		List<ResultEx> results;		
		MotifDefinition def;
		Pssm pssm;
		Wregex wregex;
		int max = 2000;
		int count;
		int i = 0;
		for( MotifInformation motifInformation : allMotis ) {
			logger.info(String.format(
				"Searching human proteome for %s motif (%d/%d) ...",
				motifInformation.getName(), ++i, allMotis.size()));
			def = motifInformation.getDefinitions().get(0);
			pssm = Services.getPssm(def.getPssm());
			wregex = new Wregex(def.getRegex(), pssm);
			resultGroups = Services.search(wregex, motifInformation, databases.getHumanProteome(), false, 0);
			results = Services.expand(resultGroups, true);
			Services.searchCosmic(databases.getMapCosmic(), results);
			Collections.sort(results);
			motif = new BubbleChartData(motifInformation.getName());
			motifs.addChild(motif);
			count = topCount;
			for( ResultEx result : results ) {
				child = motif.getChild(result.getGene());
				if( child != null ) {
					if( result.getCosmicMissense() > 0 )
						child.setSize(child.getSize()+result.getCosmicMissense());
					continue;
				}
				child = new BubbleChartData(result.getGene(),result.getCosmicMissense());
				if( child.getSize() <= 0 )
					continue;
				motif.addChild(child);
				if( --count <= 0 )
					break;
			}
			if( --max <= 0 )
				break;
		}						
		logger.info("finished!");
	}
	
	private void createJson() {
		BubbleChartData bubbles = new BubbleChartData("");
		for( BubbleChartData motif : motifs.getChildren() ) {
			if( motif.getTotalSize() < minMutations )
				continue;
			bubbles.addChild(motif);
			for( BubbleChartData gene : motif.getChildren() )
				if( gene.getSize() > maxMutations )
					gene.setSize(maxMutations);
		}
		jsonMotifs = bubbles.toString(null);
	}

	public String getJsonMotifs() {
		return jsonMotifs;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public int getTopCount() {
		return topCount;
	}

	public int getMaxMutations() {
		return maxMutations;
	}

	public int getMinMutations() {
		return minMutations;
	}
	
	public List<String> getDisplayTips() {
		return displayTips;
	}
}
