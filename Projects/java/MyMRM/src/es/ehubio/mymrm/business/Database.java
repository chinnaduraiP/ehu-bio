package es.ehubio.mymrm.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.ExperimentFile;
import es.ehubio.mymrm.data.Fragment;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.PeptideEvidence;
import es.ehubio.mymrm.data.Precursor;
import es.ehubio.mymrm.data.ScoreType;
import es.ehubio.mymrm.data.Transition;
import es.ehubio.proteomics.FragmentIon;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;

public class Database {
	private static final Logger logger = Logger.getLogger(Database.class.getName());
	private static final int FRAGMENTS=10;
	private static final int OKFRAGMENTS=3;
	private static EntityManagerFactory emf;
	private static EntityManager em;	
	
	public static void connect() {
		emf = Persistence.createEntityManagerFactory("MyMRM");
		em = emf.createEntityManager();
	}
	
	public static void close() {
		em.close();
		emf.close();
	}
	
	public static void beginTransaction() {
		em.getTransaction().begin();
	}
	
	public static void commitTransaction() {
		em.getTransaction().commit();
	}
	
	public static <T> List<T> findAll(Class<T> c) {
		return em.createQuery(String.format("SELECT x FROM %s x",c.getSimpleName()),c).getResultList();
	}
	
	public static <T> void add(T item) {
		beginTransaction();
		em.persist(item);
		commitTransaction();
	}
	
	public static <T> T findById(Class<T> c, int id) {
		return (T)em.find(c, id);
	}
	
	public static <T> boolean remove(Class<T> c, int id) {		
		T item = findById(c, id);
		if( item == null )
			return false;
		beginTransaction();
		em.remove(item);
		commitTransaction();
		return true;
	}
	
	public static List<Experiment> findExperiments() {
		List<Experiment> experiments = findAll(Experiment.class);
		for( Experiment experiment : experiments )
			experiment.setExperimentFiles(findExperimentFiles(experiment.getId()));
		return experiments;
	}
	
	public static Peptide findByMassSequence( String massSequence ) {
		Peptide peptide = null;
		try {
			peptide = em
					.createQuery("SELECT p FROM Peptide p WHERE p.massSequence = :massSequence",Peptide.class)
					.setParameter("massSequence", massSequence)
					.getSingleResult();
		} catch( NoResultException ex ) {			
		}
		return peptide;
	}
	
	public static int countPeptidesBySequence( String sequence ) {
		Number res = em.createQuery("SELECT COUNT (p) FROM Peptide p WHERE p.sequence = :sequence",Number.class)
			.setParameter("sequence", sequence)
			.getSingleResult();
		return res.intValue();
	}
	
	public static List<Peptide> findBySequence( String sequence ) {
		List<Peptide> list = null;
		try {
			list = em
					.createQuery("SELECT p FROM Peptide p WHERE p.sequence = :sequence",Peptide.class)
					.setParameter("sequence", sequence)
					.getResultList();
		} catch( NoResultException ex ) {			
		}
		return list == null ? new ArrayList<Peptide>() : list;
	}
	
	public static ScoreType findScoreTypeByName( String name ) {
		ScoreType scoreType = null;
		try {
			scoreType = em
					.createQuery("SELECT s FROM ScoreType s WHERE s.name = :name",ScoreType.class)
					.setParameter("name", name)
					.getSingleResult();
		} catch( NoResultException ex ) {			
		}
		return scoreType;
	}
	
	public static boolean feed( int experimentId, MsMsData data, es.ehubio.proteomics.Peptide.Confidence confidence ) {
		Experiment experiment = findById(Experiment.class, experimentId);
		if( experiment == null )
			return false;
		
		double total = data.getPeptides().size();
		double partial = 0.0;
		for( es.ehubio.proteomics.Peptide peptide : data.getPeptides() ) {
			partial += 1.0;
			if( Boolean.TRUE.equals(peptide.getDecoy()) || peptide.getConfidence().getOrder() > confidence.getOrder() )
				continue;
			beginTransaction();
			Peptide dbPeptide = findByMassSequence(peptide.getMassSequence());
			if( dbPeptide == null ) {
				dbPeptide = new Peptide();
				dbPeptide.setSequence(peptide.getSequence());
				dbPeptide.setMassSequence(peptide.getMassSequence());				
				em.persist(dbPeptide);				
			}			
			for( Psm psm : peptide.getPsms() ) {
				//logger.info(String.format("Feedind with %s (mz=%s)", peptide.getSequence(), psm.getCalcMz()));
				Precursor precursor = new Precursor();
				precursor.setMz(psm.getCalcMz());
				precursor.setCharge(psm.getCharge());
				precursor.setRt(psm.getSpectrum().getRt());
				precursor.setIntensity(psm.getSpectrum().getIntensity());
				em.persist(precursor);
				PeptideEvidence evidence = new PeptideEvidence();
				evidence.setPeptideBean(dbPeptide);
				evidence.setPrecursorBean(precursor);				
				evidence.setExperimentBean(experiment);
				em.persist(evidence);
				feedIons(precursor,psm.getSpectrum().getIons());
				feedScores(evidence,psm.getScores());
			}
			commitTransaction();
			if( ((int)(partial+0.5))%10 == 0 )
				logger.info(String.format("Completed %.1f%%", partial/total*100.0));
		}
		return true;
	}
	
	private static void feedIons( Precursor precursor, List<FragmentIon> ions) {
		Collections.sort(ions, new Comparator<FragmentIon>() {
			@Override
			public int compare(FragmentIon o1, FragmentIon o2) {
				return (int)Math.signum(o2.getIntensity()-o1.getIntensity());
			}
		});
		int count = FRAGMENTS;
		int countok = OKFRAGMENTS;
		for( FragmentIon ion : ions ) {
			if( count > 0 || (countok > 0 && ion.getMz() > precursor.getMz()) ) {
				Fragment fragment = new Fragment();
				fragment.setMz(ion.getMz());
				fragment.setIntensity(ion.getIntensity());
				em.persist(fragment);
				Transition transition = new Transition();
				transition.setPrecursorBean(precursor);
				transition.setFragmentBean(fragment);
				em.persist(transition);
				count--;
				if( ion.getMz() > precursor.getMz() )
					countok--;
			}			
		}
	}
	
	private static void feedScores(PeptideEvidence evidence, Set<Score> scores) {
		for( Score score : scores ) {
			ScoreType scoreType = findScoreTypeByName(score.getName());
			if( scoreType == null ) {
				scoreType = new ScoreType();
				scoreType.setName(score.getName());
				scoreType.setDescription(score.getType().getDescription());
				scoreType.setLargerBetter(score.getType().isLargerBetter());
				em.persist(scoreType);
			}
			es.ehubio.mymrm.data.Score dbScore = new es.ehubio.mymrm.data.Score();
			dbScore.setScoreType(scoreType);
			dbScore.setPeptideEvidenceBean(evidence);
			dbScore.setValue(score.getValue());
			em.persist(dbScore);
		}
	}

	public static List<Peptide> findPeptides(String pepSequence) {
		List<Peptide> peptides = findBySequence(pepSequence);
		for( Peptide peptide : peptides )
			peptide.setPeptideEvidences(findEvidences(peptide.getId()));
		return peptides;
	}

	private static List<PeptideEvidence> findEvidences(int idPeptide) {
		List<PeptideEvidence> evidences = null;
		try {
			evidences = em
					.createQuery("SELECT p FROM PeptideEvidence p WHERE p.peptideBean.id = :peptide", PeptideEvidence.class)
					.setParameter("peptide", idPeptide)
					.getResultList();
		} catch( NoResultException ex ) {			
		}
		return evidences == null ? new ArrayList<PeptideEvidence>() : evidences;
	}

	public static List<Fragment> findFragments(int idPrecursor) {
		List<Fragment> fragments = new ArrayList<>();
		try {
			List<Transition> transitions = em
					.createQuery("SELECT t FROM Transition t WHERE t.precursorBean.id = :precursor", Transition.class)
					.setParameter("precursor", idPrecursor)
					.getResultList();
			for( Transition transition : transitions )
				fragments.add(transition.getFragmentBean());
		} catch( NoResultException ex ) {			
		}
		return fragments;
	}

	public static List<es.ehubio.mymrm.data.Score> findScores(int evidenceId) {
		List<es.ehubio.mymrm.data.Score> scores = null;
		try {
			scores = em
				.createQuery("SELECT s FROM Score s WHERE s.peptideEvidenceBean.id = :evidence", es.ehubio.mymrm.data.Score.class)
				.setParameter("evidence", evidenceId)
				.getResultList();
		} catch( NoResultException ex ) {			
		}
		return scores == null ? new ArrayList<es.ehubio.mymrm.data.Score>() : scores;
	}
	
	public static List<ExperimentFile> findExperimentFiles( int idExperiment ) {
		List<ExperimentFile> files = null;
		try {
			files = em
				.createQuery("SELECT f FROM ExperimentFile f WHERE f.experimentBean.id = :exp", ExperimentFile.class)
				.setParameter("exp", idExperiment)
				.getResultList();
		} catch( NoResultException ex ) {			
		}
		return files == null ? new ArrayList<ExperimentFile>() : files;
	}
	
	public static int countExperimentFiles( int idExperiment ) {
		Number res = em.createQuery("SELECT COUNT (f) FROM ExperimentFile f WHERE f.experimentBean.id = :exp",Number.class)
			.setParameter("exp", idExperiment)
			.getSingleResult();
		return res.intValue();
	}
}