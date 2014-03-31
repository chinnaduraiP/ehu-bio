package es.ehubio.wregex.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.wregex.Pssm;
import es.ehubio.wregex.Wregex;
import es.ehubio.wregex.data.BubbleChartData;
import es.ehubio.wregex.data.DatabaseInformation;
import es.ehubio.wregex.data.MotifDefinition;
import es.ehubio.wregex.data.MotifInformation;
import es.ehubio.wregex.data.ResultEx;
import es.ehubio.wregex.data.ResultGroupEx;
import es.ehubio.wregex.data.Services;

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
	private final int minMutations = 100;
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
			DatabaseInformation bubbles = databases.getDbBubbles();
			if( bubbles != null && bubbles.exists() ) {
				logger.info("Using cached bubbles");
				loadBubbles();
			} else {
				searchHumanProteome();
				createJson();
				saveJson();
				logger.info("Bubbles saved for future uses");
			}
			initialized = true;
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void loadBubbles() throws IOException {
		Scanner scanner = new Scanner(new File(databases.getDbBubbles().getPath())); 
		jsonMotifs = scanner.useDelimiter("\\A").next();
		scanner.close();
	}

	private void searchHumanProteome() throws IOException, InvalidSequenceException, Exception {		
		List<MotifInformation> allMotis = databases.getNrMotifs();
		motifs = new BubbleChartData();
		BubbleChartData motif, child;
		List<ResultGroupEx> resultGroups;
		List<ResultEx> results;		
		MotifDefinition def;
		Pssm pssm;
		Wregex wregex;
		int max = 2000;
		int count;
		int i = 0;
		final String discretion = "COSMIC missense mutations in potencial motif candidates";
		long tout = Services.getInitNumber("wregex.watchdogtimer")*1000;
		for( MotifInformation motifInformation : allMotis ) {
			logger.info(String.format(
				"Searching human proteome for %s motif (%d/%d) ...",
				motifInformation.getName(), ++i, allMotis.size()));
			def = motifInformation.getDefinitions().get(0);
			pssm = Services.getPssm(def.getPssm());
			wregex = new Wregex(def.getRegex(), pssm);
			try {
				resultGroups = Services.search(wregex, motifInformation, databases.getHumanProteome(), false, tout);
			} catch( Exception e ) {
				logger.severe("Discarded by tout");
				continue;
			}
			results = Services.expand(resultGroups, true);
			Services.searchCosmic(databases.getMapCosmic(), results);
			Collections.sort(results);
			motif = new BubbleChartData();
			motif.setName(motifInformation.getName());
			motif.setDescription(motifInformation.getSummary());
			motif.setDiscretion(discretion);			
			count = topCount;
			for( ResultEx result : results ) {
				if( result.getGene() == null )
					continue;
				child = motif.getChild(result.getGene());
				if( child != null ) {
					if( result.getCosmicMissense() > 0 )
						child.setSize(child.getSize()+result.getCosmicMissense());
					continue;
				}
				child = new BubbleChartData();
				child.setName(result.getGene());
				child.setDescription(result.getFasta().getDescription());
				child.setDiscretion(discretion);
				child.setResult(""+result.getCosmicMissense());
				child.setSize(result.getCosmicMissense());
				if( child.getSize() <= 0 )
					continue;
				motif.addChild(child);
				if( --count <= 0 )
					break;
			}
			if( motif.getChildren().isEmpty() )
				continue;
			motif.setResult(""+motif.getChildsSize());
			motifs.addChild(motif);
			if( --max <= 0 )
				break;
		}						
		logger.info("finished!");
	}
	
	private void createJson() {
		BubbleChartData bubbles = new BubbleChartData();
		for( BubbleChartData motif : motifs.getChildren() ) {
			if( motif.getTotalSize() < minMutations )
				continue;
			bubbles.addChild(motif);
			for( BubbleChartData gene : motif.getChildren() ) {				
				if( gene.getSize() > maxMutations )
					gene.setSize(maxMutations);
			}
		}
		jsonMotifs = bubbles.toString(null);
	}
	
	private void saveJson() {
		DatabaseInformation db = databases.getDbBubbles();
		if( db == null )
			return;
		try {
			PrintWriter pw = new PrintWriter(db.getPath());
			pw.print(jsonMotifs);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
