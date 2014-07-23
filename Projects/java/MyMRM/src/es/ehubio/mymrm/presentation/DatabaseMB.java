package es.ehubio.mymrm.presentation;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.ehubio.mymrm.business.Database;
import es.ehubio.mymrm.data.Instrument;

@ManagedBean
@ApplicationScoped
public class DatabaseMB {	
	public DatabaseMB() {
		Database.connect();
	}
	
	public List<Instrument> getInstruments() {
		return Database.findAll(Instrument.class);
	}
	
	@Override
	protected void finalize() throws Throwable {
		Database.close();
		super.finalize();
	}
}
