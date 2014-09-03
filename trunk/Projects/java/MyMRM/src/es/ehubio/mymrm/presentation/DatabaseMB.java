package es.ehubio.mymrm.presentation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import es.ehubio.mymrm.business.Database;
import es.ehubio.mymrm.data.Chromatography;
import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.FastaFile;
import es.ehubio.mymrm.data.Fragment;
import es.ehubio.mymrm.data.FragmentationType;
import es.ehubio.mymrm.data.Instrument;
import es.ehubio.mymrm.data.InstrumentType;
import es.ehubio.mymrm.data.IonizationType;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.Score;
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
		experiment.setIonizationTypeBean(Database.findById(IonizationType.class, Integer.parseInt(bean.getIonization())));
		experiment.setFragmentationTypeBean(Database.findById(FragmentationType.class, Integer.parseInt(bean.getFragmentation())));
		experiment.setChromatographyBean(Database.findById(Chromatography.class, Integer.parseInt(bean.getChromatography())));
		Database.add(experiment);
	}
	
	public List<FragmentationType> getFragmentationTypes() {
		return Database.findAll(FragmentationType.class);
	}
	
	public List<FragmentationType> getFragmentationTypesNull() {
		List<FragmentationType> list = new ArrayList<>(getFragmentationTypes());
		list.add(null);
		return list;
	}
	
	public void removeFragmentationType( FragmentationType type ) {
		Database.remove(FragmentationType.class, type.getId());
	}
	
	public void addFragmentationType( FragmentationTypeMB bean ) {
		Database.add(bean.getEntity());
	}
	
	public List<IonizationType> getIonizationTypes() {
		return Database.findAll(IonizationType.class);
	}
	
	public List<IonizationType> getIonizationTypesNull() {
		List<IonizationType> list = new ArrayList<>(getIonizationTypes());
		list.add(null);
		return list;
	}
	
	public void removeIonizationType( IonizationType type ) {
		Database.remove(IonizationType.class, type.getId());
	}
	
	public void addIonizationType( IonizationTypeMB bean ) {
		Database.add(bean.getEntity());
	}
	
	public List<FastaFile> getFastas() {
		List<FastaFile> list = new ArrayList<>();
		File dir = new File(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("MyMRM.fastaDir"));
		for( File file : dir.listFiles("*fasta*") )
		return list;
	}
	
	public void removeFasta( FastaFile fasta ) {
		Database.remove(FastaFile.class, fasta.getId());
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
			Database.feed(experiment.getId(), panalyzer.getData(), es.ehubio.proteomics.Peptide.Confidence.DISCRIMINATING);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public List<Fragment> getFragments( int idPrecursor ) {
		return Database.findFragments( idPrecursor );
	}
	
	public List<Peptide> search( String pepSequence ) {
		return Database.findPeptides( pepSequence );
	}
	
	@Override
	protected void finalize() throws Throwable {
		Database.close();
		super.finalize();
	}

	public List<Score> getScores(int evidenceId) {
		return Database.findScores(evidenceId);
	}	
}
