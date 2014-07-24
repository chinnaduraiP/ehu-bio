package es.ehubio.mymrm.business;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import es.ehubio.mymrm.data.Experiment;
import es.ehubio.mymrm.data.Fragment;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.mymrm.data.Precursor;
import es.ehubio.mymrm.data.Transition;
import es.ehubio.proteomics.FragmentIon;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;

public class Database {
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
	
	public static boolean feed( int experimentId, MsMsData data ) {
		Experiment experiment = findById(Experiment.class, experimentId);
		if( experiment == null )
			return false;
		
		for( es.ehubio.proteomics.Peptide peptide : data.getPeptides() ) {
			beginTransaction();
			Peptide dbPeptide = findByMassSequence(peptide.getMassSequence());
			if( dbPeptide == null ) {
				dbPeptide = new Peptide();
				dbPeptide.setSequence(peptide.getSequence());
				dbPeptide.setMassSequence(peptide.getMassSequence());				
				em.persist(dbPeptide);				
			}			
			for( Psm psm : peptide.getPsms() ) {
				Precursor precursor = new Precursor();
				precursor.setMz(psm.getExpMz());
				precursor.setCharge(psm.getCharge());
				precursor.setPeptideBean(dbPeptide);
				em.persist(precursor);
				for( FragmentIon ion : psm.getSpectrum().getIons() ) {
					Fragment fragment = new Fragment();
					fragment.setMz(ion.getMz());
					fragment.setIntensity(ion.getIntensity());
					em.persist(fragment);
					Transition transition = new Transition();
					transition.setExperimentBean(experiment);
					transition.setPrecursorBean(precursor);
					transition.setFragmentBean(fragment);
					em.persist(transition);
				}
			}
			commitTransaction();
		}
		return true;
	}

	public static List<Peptide> search(String pepSequence) {
		//List<Transition> result = new ArrayList<>();
		List<Peptide> peptides = findBySequence(pepSequence);
		/*for( Peptide peptide : peptides ) {
			List<Precursor> precursors = findPrecursors(peptide);
		}
		return result;*/
		return peptides;
	}

	private static List<Precursor> findPrecursors(Peptide peptide) {
		List<Precursor> precursors = null;
		try {
			precursors = em
					.createQuery("SELECT p FROM Precursor WHERE p.petideBean = :peptide", Precursor.class)
					.setParameter("peptide", peptide)
					.getResultList();
		} catch( NoResultException ex ) {			
		}
		return precursors == null ? new ArrayList<Precursor>() : precursors;
	}
}