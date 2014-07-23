package es.ehubio.mymrm.business;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findAll(Class<T> c) {
		return em.createNamedQuery(String.format("%s.findAll",c.getSimpleName())).getResultList();
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
}