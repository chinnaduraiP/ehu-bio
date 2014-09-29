package es.ehubio.panalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import es.ehubio.panalyzer.html.HtmlReport;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.EhubioCsv;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.io.Mzid;
import es.ehubio.proteomics.pipeline.Filter;
import es.ehubio.proteomics.pipeline.PAnalyzer;
import es.ehubio.proteomics.pipeline.Validator;
import es.ehubio.proteomics.pipeline.Validator.FdrResult;

public class MainModel {
	public enum State { INIT, CONFIGURED, LOADED, RESULTS, SAVED}
	public static final String NAME = "PAnalyzer";
	public static final String VERSION = "v2.0-alpha1";
	public static final String SIGNATURE = String.format("%s (%s)", NAME, VERSION);	

	private static final Logger logger = Logger.getLogger(MainModel.class.getName());
	private static final String STATE_ERR_MSG="This method should not be called in the current state";
	private static final int MAXITER=15;
	private String status;
	private MsMsData data;
	private MsMsFile file;
	private Configuration config;
	private State state;
	private Set<ScoreType> psmScoreTypes;
	
	private PAnalyzer pAnalyzer;
	private Validator validator;
	
	public MainModel() {
		resetTotal();	
	}
	
	public void run() {
		resetData();
		loadData();
		filterData();
		saveData();
	}
	
	public void run( String pax ) throws JAXBException {
		resetTotal();
		loadConfig(pax);
		run();
	}
	
	private void resetData() {
		data = null;
		psmScoreTypes = null;
		status = "Experiment configured, you can now load the data";
		state = State.CONFIGURED;		
	}

	private void resetTotal() {
		resetData();
		config = null;		
		status = "Load experiment data";
		state = State.INIT;
	}
	
	public void reset() {
		resetTotal();
		logger.info("--- Started a new analysis ---");
	}
	
	public State getState() {
		return state;
	}

	public Configuration getConfig() {
		return config;
	}
	
	public void setConfig( Configuration config ) {
		if( config == null ) {
			resetTotal();
			return;
		}
		resetData();
		this.config = config;
	}
	
	public void loadConfig( String path ) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		Configuration config = (Configuration)um.unmarshal(new File(path));		
		logger.info(String.format("Using config from '%s': %s", path, config.getDescription()));
		setConfig(config);
	}
	
	public void loadData() {
		assertState(state==State.CONFIGURED);
		try {
			MsMsData tmp;
			for( String input : config.getInputs() ) {
				file = new Mzid();		
				tmp = file.load(input,config.getDecoyRegex());
				if( data == null ) {
					data = tmp;
					logCounts("Loaded");
				} else {
					data.merge(tmp);
					logCounts("Merged");
				}
			}
			pAnalyzer = new PAnalyzer(data);
			validator = new Validator(data);
			rebuildGroups();
			status = "Data loaded, you can now apply a filter";
			state = State.LOADED;
		} catch( Exception e ) {
			resetData();
			handleException(e, "Error loading data, correct your configuration");
		}
	}
	
	public Set<ScoreType> getPsmScoreTypes() {
		assertState(state.ordinal()>=State.LOADED.ordinal());
		if( psmScoreTypes == null ) {
			psmScoreTypes = new HashSet<>();
			for( Psm psm : data.getPsms() ) {
				if( psm.getScores().isEmpty() )
					continue;
				for( Score score : psm.getScores() )
					psmScoreTypes.add(score.getType());
				break;
			}
		}
		return psmScoreTypes;
	}
	
	public void filterData() {
		assertState(state == State.LOADED || state == State.RESULTS);
		try {
			inputFilter();
			processPsmFdr();
			processPeptideFdr();
			processProteinFdr();
			processGroupFdr();
			validator.logFdrs();
			logCounts("Final counts");
			logger.info(getCounts().toString());
			status = "Data filtered, you can now save the results";
			state = State.RESULTS;
		} catch( Exception e ) {
			resetData();
			handleException(e, "Error filtering data, correct your configuration");
		}
	}
	
	public File saveData() {
		try {
			if( Boolean.TRUE.equals(config.getFilterDecoys()) ) {
				Filter filter = new Filter(data);
				filter.setFilterDecoyPeptides(true);
				filterAndGroup(filter,"Decoy removal");
			}
			if( config.getOutput() == null || config.getOutput().isEmpty() )
				return null;
			File dir = new File(config.getOutput());
			dir.mkdir();
			if( config.getInputs().size() == 1 )
				file.save(config.getOutput());
			EhubioCsv csv = new EhubioCsv(data);
			csv.setPsmScoreType(config.getPsmScore());
			csv.save(config.getOutput());
			saveConfiguration();
			File file = generateHtml();
			status = "Data saved, you can now browse the results";
			state = State.SAVED;
			return file;
		} catch( Exception e ) {
			handleException(e, "Error saving data, correct your configuration");			
		}
		return null;
	}	

	public MsMsData getData() {
		return data;
	}
	
	public String getStatus() {
		return status;
	}
	
	public PAnalyzer.Counts getCounts() {
		return pAnalyzer.getCounts();
	}
	
	public PAnalyzer.Counts getTargetCounts() {
		return pAnalyzer.getTargetCounts();
	}
	
	public PAnalyzer.Counts getDecoyCounts() {
		return pAnalyzer.getDecoyCounts();
	}
	
	public FdrResult getPsmFdr() {
		return validator.getPsmFdr();
	}
	
	public FdrResult getPeptideFdr() {
		return validator.getPeptideFdr();
	}
	
	public FdrResult getProteinFdr() {
		return validator.getProteinFdr();
	}
	
	public FdrResult getGroupFdr() {
		return validator.getGroupFdr();
	}
	
	private void saveConfiguration() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Marshaller marshaller = context.createMarshaller();
		File pax = new File(getConfig().getOutput(),"config.pax");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(getConfig(), pax);
		logger.info(String.format("Config saved in '%s'", pax.getName()));
	}
	
	private File generateHtml() throws IOException {
		HtmlReport html = new HtmlReport(this);
		html.create();
		logger.info(String.format("HTML report available in '%s'", html.getHtmlFile().getName()));
		return html.getHtmlFile();
	}	
	
	private void logCounts( String title ) {
		logger.info(String.format("%s: %s", title, data.toString()));
	}
	
	private void assertState( boolean ok ) {
		if( !ok )
			throw new AssertionError(STATE_ERR_MSG);
	}
	
	private void handleException( Exception e, String msg ) {
		e.printStackTrace();
		status = msg;
		logger.severe(String.format("%s: %s", msg, e.getMessage()));
	}
	
	private void rebuildGroups() {
		//logger.info("Updating protein groups ...");
		pAnalyzer.run();
		logger.info("Re-grouped: "+getCounts().toString());
	}
	
	private void filterAndGroup( Filter filter, String title ) {
		filter.run();
		logCounts(title);
		rebuildGroups();
	}
	
	private void inputFilter() {
		validator.logFdrs();
		Filter filter = new Filter(data);
		filter.setRankTreshold(config.getPsmRankThreshold()==null?0:config.getPsmRankThreshold());
		filter.setOnlyBestPsmPerPrecursor(Boolean.TRUE.equals(config.getBestPsmPerPrecursor())?config.getPsmScore():null);
		filter.setMinPeptideLength(config.getMinPeptideLength()==null?0:config.getMinPeptideLength());
		filter.setFilterDecoyPeptides(false);
		filterAndGroup(filter,"Input filter");
		validator.logFdrs();
	}
	
	private void processPsmFdr() {
		if( config.getPsmFdr() != null || config.getPeptideFdr() != null || config.getProteinFdr() != null || config.getGroupFdr() != null )
			validator.updatePsmDecoyScores(config.getPsmScore());
		if( config.getPsmFdr() == null )
			return;
		Filter filter = new Filter(data);
		filter.setPsmScoreThreshold(new Score(ScoreType.PSM_Q_VALUE, config.getPsmFdr()));
		filterAndGroup(filter,String.format("PSM FDR=%s filter",config.getPsmFdr()));
	}
	
	private void processPeptideFdr() {
		if( config.getPeptideFdr() != null || config.getProteinFdr() != null || config.getGroupFdr() != null ) {
			validator.updatePeptideProbabilities();
			validator.updatePeptideDecoyScores(ScoreType.PEPTIDE_P_VALUE);
		}
		if( config.getPeptideFdr() == null )
			return;
		Filter filter = new Filter(data);
		filter.setPeptideScoreThreshold(new Score(ScoreType.PEPTIDE_Q_VALUE, config.getPeptideFdr()));
		filterAndGroup(filter,String.format("Peptide FDR=%s filter",config.getPeptideFdr()));
	}
	
	private void processProteinFdr() {
		if( config.getProteinFdr() == null )
			return;
		validator.updateProteinProbabilities();
		validator.updateProteinDecoyScores(ScoreType.PROTEIN_P_VALUE);
		Filter filter = new Filter(data);
		filter.setProteinScoreThreshold(new Score(ScoreType.PROTEIN_Q_VALUE, config.getProteinFdr()));
		filterAndGroup(filter,String.format("Protein FDR=%s filter",config.getProteinFdr()));
	}
	
	private void processGroupFdr() {
		if( config.getGroupFdr() == null )
			return;
		
		PAnalyzer.Counts curCount = pAnalyzer.getCounts(), prevCount;
		int i = 0;
		Filter filter = new Filter(data);
		filter.setGroupScoreThreshold(new Score(ScoreType.GROUP_Q_VALUE, config.getGroupFdr()));
		do {
			i++;
			validator.updateGroupProbabilities();
			validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
			filterAndGroup(filter,String.format("Group FDR=%s filter, iteration %s",config.getGroupFdr(),i));
			prevCount = curCount;
			curCount = pAnalyzer.getCounts();
		} while( !curCount.equals(prevCount) && i < MAXITER );
		if( i >= MAXITER )
			logger.warning("Maximum number of iterations reached!");
		
		validator.updateGroupProbabilities();
		validator.updateGroupDecoyScores(ScoreType.GROUP_P_VALUE);
	}
}