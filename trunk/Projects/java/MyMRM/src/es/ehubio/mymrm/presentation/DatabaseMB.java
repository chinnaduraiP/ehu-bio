package es.ehubio.mymrm.presentation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.ehubio.mymrm.business.Database;
import es.ehubio.mymrm.data.Chromatography;
import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.Instrument;
import es.ehubio.mymrm.data.InstrumentType;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.Transition;
import es.ehubio.tools.PAnalyzerCli;

@ManagedBean
@ApplicationScoped
public class DatabaseMB {	
	public DatabaseMB() {
		Database.connect();
	}
	
	public List<Instrument> getInstruments() {
		return Database.findAll(Instrument.class);
	}
	
	public List<Instrument> getInstrumentsNull() {
		List<Instrument> list = new ArrayList<>(getInstruments());
		list.add(null);
		return list;
	}
	
	public void removeInstrument( Instrument instrument ) {
		Database.remove(Instrument.class, instrument.getId());
	}
	
	public void addInstrument( InstrumentMB bean ) {
		Instrument instrument = bean.getEntity();
		instrument.setInstrumentTypeBean(Database.findById(InstrumentType.class, Integer.parseInt(bean.getTypeId())));
		Database.add(instrument);
	}
	
	public List<InstrumentType> getInstrumentTypes() {
		return Database.findAll(InstrumentType.class);
	}
	
	public List<InstrumentType> getInstrumentTypesNull() {
		List<InstrumentType> list = new ArrayList<>(getInstrumentTypes());
		list.add(null);
		return list;
	}
	
	public void removeInstrumentType( InstrumentType type ) {
		Database.remove(InstrumentType.class, type.getId());
	}
	
	public void addInstrumentType( InstrumentTypeMB bean ) {
		Database.add(bean.getEntity());
	}
	
	public List<Chromatography> getChromatographies() {
		return Database.findAll(Chromatography.class);
	}
	
	public List<Chromatography> getChromatograhiesNull() {
		List<Chromatography> list = new ArrayList<>(getChromatographies());
		list.add(null);
		return list;
	}
	
	public void removeChromatography( Chromatography chr ) {
		Database.remove(Chromatography.class, chr.getId());
	}
	
	public void addChromatography( ChromatographyMB bean ) {
		Database.add(bean.getEntity());
	}
	
	public List<Experiment> getExperiments() {
		return Database.findAll(Experiment.class);
	}
	
	public List<Experiment> getExperimentsNull() {
		List<Experiment> list = new ArrayList<>(getExperiments());
		list.add(null);
		return list;
	}
	
	public void removeExperiment( Experiment exp ) {
		Database.remove(Experiment.class, exp.getId());
	}
	
	public void addExperiment( ExperimentMB bean ) {
		Experiment experiment = bean.getEntity();
		experiment.setInstrumentBean(Database.findById(Instrument.class, Integer.parseInt(bean.getInstrument())));
		experiment.setChromatographyBean(Database.findById(Chromatography.class, Integer.parseInt(bean.getChromatography())));
		Database.add(experiment);
	}
	
	public void feed( ExperimentMB bean ) {
		Experiment experiment = Database.findById(Experiment.class, Integer.parseInt(bean.getId()));
		if( experiment == null )
			return;
		try {
			PAnalyzerCli panalyzer = new PAnalyzerCli();
			panalyzer.setLoadIons(true);
			panalyzer.setSaveResults(false);
			String[] args = {bean.getPax()};
			panalyzer.run(args);		
			Database.feed(experiment.getId(), panalyzer.getData());
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public List<Peptide> search( String pepSequence ) {
		return Database.search( pepSequence );
	}
	
	@Override
	protected void finalize() throws Throwable {
		Database.close();
		super.finalize();
	}
}
