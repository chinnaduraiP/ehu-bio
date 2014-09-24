package es.ehubio.panalyzer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.io.MsMsFile;
import es.ehubio.proteomics.io.Mzid;

public class MainModel {
	public enum State { INIT, CONFIGURED, LOADED, RESULTS, SAVED}
	public static final String NAME = "PAnalyzer";
	public static final String VERSION = "v2.0-alpha1";
	public static final String SIGNATURE = String.format("%s (%s)", NAME, VERSION);

	private static final Logger logger = Logger.getLogger(MainModel.class.getName());
	private static final String STATE_ERR_MSG="This method should not be called in the current state";
	private String status;
	private MsMsData data;
	private MsMsFile file;
	private Configuration config;
	private State state;
	private Set<ScoreType> psmScoreTypes;
	
	public MainModel() {
		resetAux();	
	}	

	private void resetAux() {		
		config = null;
		data = null;
		psmScoreTypes = null;
		status = "Load experiment data";
		state = State.INIT;
	}
	
	public void reset() {
		resetAux();
		logger.info("--- Started a new analysis ---");
	}
	
	public State getState() {
		return state;
	}

	public Configuration getConfig() {
		return config;
	}
	
	public void setConfig( Configuration config ) {
		resetAux();
		this.config = config;
		if( config == null )
			return;
		state = State.CONFIGURED;
		status = "Experiment configured, you can now load the data";
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
			status = "Data loaded, you can now apply a filter";
			state = State.LOADED;
		} catch( Exception e ) {
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
			status = "Data filtered, you can now save the results";
			state = State.RESULTS;
		} catch( Exception e ) {
			handleException(e, "Error filtering data, correct your configuration");
		}
	}
	
	public MsMsData getData() {
		return data;
	}
	
	public String getStatus() {
		return status;
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
		resetAux();
		status = msg;
		logger.severe(String.format("%s: %s", msg, e.getMessage()));
	}
}