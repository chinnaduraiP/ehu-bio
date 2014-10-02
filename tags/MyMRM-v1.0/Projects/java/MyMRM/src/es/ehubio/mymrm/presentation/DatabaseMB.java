package es.ehubio.mymrm.presentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehubio.mymrm.business.Database;
import es.ehubio.mymrm.business.ExperimentFeed;
import es.ehubio.mymrm.data.Chromatography;
import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.ExperimentFile;
import es.ehubio.mymrm.data.FastaFile;
import es.ehubio.mymrm.data.Fragment;
import es.ehubio.mymrm.data.FragmentationType;
import es.ehubio.mymrm.data.Instrument;
import es.ehubio.mymrm.data.InstrumentType;
import es.ehubio.mymrm.data.IonizationType;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.Score;
import es.ehubio.proteomics.ScoreType;

@ManagedBean
@ApplicationScoped
public class DatabaseMB {	
	public DatabaseMB() {
		Database.connect();
	}
	
	public List<Instrument> getInstruments() {
		Database.beginTransaction();
		List<Instrument> list = Database.findAll(Instrument.class);
		Database.commitTransaction();
		return list;
	}
	
	public List<Instrument> getInstrumentsNull() {
		List<Instrument> list = new ArrayList<>(getInstruments());
		list.add(null);
		return list;
	}
	
	public void removeInstrument( Instrument instrument ) {
		Database.beginTransaction();
		Database.remove(Instrument.class, instrument.getId());
		Database.commitTransaction();
	}
	
	public void addInstrument( InstrumentMB bean ) {
		Instrument instrument = bean.getEntity();
		Database.beginTransaction();
		instrument.setInstrumentTypeBean(Database.findById(InstrumentType.class, Integer.parseInt(bean.getTypeId())));
		Database.add(instrument);
		Database.commitTransaction();
	}
	
	public List<InstrumentType> getInstrumentTypes() {
		Database.beginTransaction();
		List<InstrumentType> list = Database.findAll(InstrumentType.class);
		Database.commitTransaction();
		return list;
	}
	
	public List<InstrumentType> getInstrumentTypesNull() {
		List<InstrumentType> list = new ArrayList<>(getInstrumentTypes());
		list.add(null);
		return list;
	}
	
	public void removeInstrumentType( InstrumentType type ) {
		Database.beginTransaction();
		Database.remove(InstrumentType.class, type.getId());
		Database.commitTransaction();
	}
	
	public void addInstrumentType( InstrumentTypeMB bean ) {
		Database.beginTransaction();
		Database.add(bean.getEntity());
		Database.commitTransaction();
	}
	
	public List<Chromatography> getChromatographies() {
		Database.beginTransaction();
		List<Chromatography> list = Database.findAll(Chromatography.class);
		Database.commitTransaction();
		return list;
	}
	
	public List<Chromatography> getChromatograhiesNull() {
		List<Chromatography> list = new ArrayList<>(getChromatographies());
		list.add(null);
		return list;
	}
	
	public void removeChromatography( Chromatography chr ) {
		Database.beginTransaction();
		Database.remove(Chromatography.class, chr.getId());
		Database.commitTransaction();
	}
	
	public void addChromatography( ChromatographyMB bean ) {
		Database.beginTransaction();
		Database.add(bean.getEntity());
		Database.commitTransaction();
	}
	
	public List<ExperimentBean> getExperiments() {
		List<ExperimentBean> list = new ArrayList<>();
		//Database.beginTransaction();
		for( Experiment experiment : Database.findExperiments() ) {
			ExperimentBean bean = new ExperimentBean();
			bean.setEntity(experiment);
			list.add(bean);
		}
		//Database.commitTransaction();
		for( ExperimentFeed feed : Database.getPendingExperiments() ) {
			ExperimentBean bean = new ExperimentBean();
			bean.setFeed(feed);
			list.add(bean);
		}
		return list;
	}
	
	public void removeExperiment( ExperimentBean exp ) {
		if( exp.getFeed() == null ) {
			Database.beginTransaction();
			Database.removeExperiment(exp.getEntity().getId());
			Database.commitTransaction();
		} else
			try {
				Database.cancelFeed(exp.getFeed());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	public List<FragmentationType> getFragmentationTypes() {
		Database.beginTransaction();
		List<FragmentationType> list = Database.findAll(FragmentationType.class);
		Database.commitTransaction();
		return list;
	}
	
	public List<FragmentationType> getFragmentationTypesNull() {
		List<FragmentationType> list = new ArrayList<>(getFragmentationTypes());
		list.add(null);
		return list;
	}
	
	public void removeFragmentationType( FragmentationType type ) {
		Database.beginTransaction();
		Database.remove(FragmentationType.class, type.getId());
		Database.commitTransaction();
	}
	
	public void addFragmentationType( FragmentationTypeMB bean ) {
		Database.beginTransaction();
		Database.add(bean.getEntity());
		Database.commitTransaction();
	}
	
	public List<IonizationType> getIonizationTypes() {
		Database.beginTransaction();
		List<IonizationType> list = Database.findAll(IonizationType.class);
		Database.commitTransaction();
		return list;
	}
	
	public List<IonizationType> getIonizationTypesNull() {
		List<IonizationType> list = new ArrayList<>(getIonizationTypes());
		list.add(null);
		return list;
	}
	
	public void removeIonizationType( IonizationType type ) {
		Database.beginTransaction();
		Database.remove(IonizationType.class, type.getId());
		Database.commitTransaction();
	}
	
	public void addIonizationType( IonizationTypeMB bean ) {
		Database.beginTransaction();
		Database.add(bean.getEntity());
		Database.commitTransaction();
	}
	
	public List<FastaFile> getFastas() {
		List<FastaFile> list = new ArrayList<>();
		File dir = new File(getFastaDir());
		for( File file : dir.listFiles() )
			if( file.isFile() && file.getName().contains("fasta") ) {
				FastaFile fasta = new FastaFile();
				fasta.setName(file.getName());
				list.add(fasta);
			}
		return list;
	}
	
	public void removeFasta( FastaFile fasta ) {
		File file = new File(getFastaDir(),fasta.getName());
		file.delete();
	}
	
	public void uploadFasta( FileUploadEvent event ) {
		try {
			UploadedFile file = event.getFile();
			InputStream is = file.getInputstream();
			OutputStream os = new FileOutputStream(new File(getFastaDir(), file.getFileName()));
			IOUtils.copy(is, os);
			is.close();
			os.close();
		} catch( Exception e ) {			
		}
	}
	
	public List<Fragment> getFragments( int idPrecursor ) {
		Database.beginTransaction();
		List<Fragment> list = Database.findFragments( idPrecursor );
		Database.commitTransaction();
		return list;
	}
	
	public List<Peptide> search( String pepSequence ) {
		Database.beginTransaction();
		List<Peptide> list = Database.findPeptides( pepSequence );
		Database.commitTransaction();
		return list;
	}
	
	public int checkPeptideAvailable( String pepSequence ) {
		Database.beginTransaction();
		int count = Database.countPeptidesBySequence(pepSequence);
		Database.commitTransaction();
		return count;
	}
	
	@Override
	protected void finalize() throws Throwable {
		Database.close();
		super.finalize();
	}

	public List<Score> getScores(int evidenceId) {
		Database.beginTransaction();
		List<Score> list = Database.findScores(evidenceId);
		Database.commitTransaction();
		return list;
	}
	
	public List<String> getScoreTypes() {
		if( scoreTypes != null )
			return scoreTypes;
		scoreTypes = new ArrayList<>();
		for( ScoreType type : ScoreType.class.getEnumConstants() ) {
			if( type == ScoreType.OTHER_LARGER )
				break;
			scoreTypes.add(type.getName());
		}
		return scoreTypes;
	}
	
	public static String getFastaDir() {
		String dir = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("MyMRM.fastaDir");
		if( dir == null )
			dir = System.getProperty("java.io.tmpdir");
		return dir;
	}
	
	public List<ExperimentFile> findExperimentFiles( int idExperiment ) {
		Database.beginTransaction();
		List<ExperimentFile> list = Database.findExperimentFiles(idExperiment);
		Database.commitTransaction();
		return list;
	}
	
	public int countExperimentFiles( int idExperiment ) {
		Database.beginTransaction();
		int count = Database.countExperimentFiles(idExperiment);
		Database.commitTransaction();
		return count;
	}
	
	public es.ehubio.proteomics.Peptide.Confidence[] getPeptideConfidences() {
		return es.ehubio.proteomics.Peptide.Confidence.values();
	}
	
	private List<String> scoreTypes;
}