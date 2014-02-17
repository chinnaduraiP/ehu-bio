package es.ehubio.wregex.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import es.ehubio.cosmic.Loci;
import es.ehubio.db.Fasta.InvalidSequenceException;
import es.ehubio.dbptm.ProteinPtms;
import es.ehubio.io.UnixCfgReader;
import es.ehubio.wregex.InputGroup;

@ManagedBean
@ApplicationScoped
public class DatabasesBean {
	private static final String WregexMotifsPath = "/resources/data/motifs.xml";
	private static final String DatabasesPath = "/resources/data/databases.xml";
	
	private MotifConfiguration motifConfiguration;
	private DatabaseConfiguration databaseConfiguration;
	private List<MotifInformation> elmMotifs;
	private List<DatabaseInformation> targets;
	private DatabaseInformation elm;
	private DatabaseInformation cosmic;
	private DatabaseInformation dbPtm;
	private Map<String,FastaDb> mapFasta;
	private Map<String,Loci> mapCosmic;
	private Map<String, ProteinPtms> mapDbPtm;
	private long lastModifiedCosmic;
	private long lastModifiedElm;
	private long lastModifiedDbPtm;
	
	private class FastaDb {
		public long lastModified;
		public List<InputGroup> entries;
	}
	
	public DatabasesBean() {
		try {
			loadDatabases();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private void loadDatabases() throws IOException, InvalidSequenceException {
		// Wregex motifs
		Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(WregexMotifsPath)); 		
		motifConfiguration = MotifConfiguration.load(rd);
		rd.close();
		
		// Databases
		rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(DatabasesPath)); 		
		databaseConfiguration = DatabaseConfiguration.load(rd);
		rd.close();
		mapFasta = new HashMap<>();
		targets = new ArrayList<>();
		for( DatabaseInformation database : databaseConfiguration.getDatabases() ) {
			if( database.getType().equals("elm") ) {
				elm = database;
				loadElmMotifs();
				continue;
			}
			if( database.getType().equals("cosmic") ) {
				cosmic = database;
				loadCosmic();
				continue;
			}
			if( database.getType().equals("dbptm") ) {
				dbPtm = database;
				loadDbPtm();
				continue;
			}
			targets.add(database);
			if( !database.getType().equals("fasta") )
				continue;
			FastaDb fasta = new FastaDb();			
			File f = new File(database.getPath());
			fasta.lastModified = f.lastModified();
			fasta.entries = loadFasta(database.getPath());
			mapFasta.put(database.getPath(), fasta);			
		}
	}
	
	private List<InputGroup> loadFasta( String path ) throws IOException, InvalidSequenceException {
		Reader rd;
		if( path.endsWith("gz") )
			rd = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
		else
			rd = new FileReader(path);
		List<InputGroup> result = InputGroup.readEntries(rd);
		rd.close();
		System.out.println("Loaded DB: " + path);
		return result;
	}
	
	public List<MotifInformation> getWregexMotifs() {
		return motifConfiguration.getMotifs();
	}
	
	public List<DatabaseInformation> getTargets() {
		return targets;
	}
	
	public List<MotifInformation> getElmMotifs() {
		File file = new File(elm.getPath());
		if( file.lastModified() != lastModifiedElm )
			try {
				loadElmMotifs();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return elmMotifs;
	}
	
	public List<InputGroup> getFasta( String path ) throws IOException, InvalidSequenceException {		
		File file = new File(path);
		FastaDb fasta = mapFasta.get(path);
		if( fasta == null ) {
			fasta = new FastaDb();
			fasta.lastModified = -1;
			mapFasta.put(path, fasta);
		}
		if( fasta.lastModified != file.lastModified() ) {
			fasta.entries = loadFasta(path);
			fasta.lastModified = file.lastModified();
		}
		return fasta.entries;
	}
	
	public DatabaseInformation getElmInformation() {
		return elm;
	}
	
	public DatabaseInformation getCosmicInformation() {
		return cosmic;
	}
	
	public DatabaseInformation getDbPtmInformation() {
		return dbPtm;
	}
	
	public Map<String,Loci> getMapCosmic() {
		File file = new File(cosmic.getPath());
		if( file.lastModified() != lastModifiedCosmic ) {
			try {
				loadCosmic();				
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
		return mapCosmic;
	}
	
	public Map<String, ProteinPtms> getMapDbPtm() {
		File file = new File(dbPtm.getPath());
		if( file.lastModified() != lastModifiedDbPtm ) {
			try {
				loadDbPtm();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mapDbPtm;
	}
	
	private void loadElmMotifs() throws IOException {
		elmMotifs = new ArrayList<>();
		MotifInformation motif;
		MotifDefinition definition;
		List<MotifDefinition> definitions;
		MotifReference reference;
		List<MotifReference> references;
		File elmFile = new File(elm.getPath());
		UnixCfgReader rd = new UnixCfgReader(new FileReader(elmFile));
		String line;
		String[] fields;
		boolean first = true;
		while( (line=rd.readLine()) != null ) {
			if( first == true ) {
				first = false;
				continue;
			}
			fields = line.replaceAll("\"","").split("\t");
			motif = new MotifInformation();
			motif.setName(fields[1]);
			motif.setSummary(fields[2]);
			definition = new MotifDefinition();
			definition.setName(fields[0]);
			definition.setDescription("ELM regular expression without using Wregex capturing groups and PSSM capabilities");
			definition.setRegex(fields[3].replaceAll("\\(", "(?:"));
			definitions = new ArrayList<>();
			definitions.add(definition);
			motif.setDefinitions(definitions);
			reference = new MotifReference();
			reference.setName("Original ELM entry");
			reference.setLink("http://elm.eu.org/elms/elmPages/"+fields[1]+".html");
			references = new ArrayList<>();
			references.add(reference);
			motif.setReferences(references);
			elmMotifs.add(motif);
		}
		rd.close();
		lastModifiedElm = elmFile.lastModified();
		String version = rd.getComment("ELM_Classes_Download_Date");
		if( version != null )
			elm.setVersion(version.split(" ")[1]);
		System.out.println("Loaded DB: " + elm.getFullName());
	}
	
	private void loadCosmic() throws FileNotFoundException, IOException {
		mapCosmic = Loci.load(cosmic.getPath());
		lastModifiedCosmic = new File(cosmic.getPath()).lastModified();
		System.out.println("Loaded DB: " + cosmic.getFullName());
	}
	
	private void loadDbPtm() throws IOException {
		mapDbPtm = ProteinPtms.load(dbPtm.getPath());
		lastModifiedDbPtm = new File(dbPtm.getPath()).lastModified();
		System.out.println("Loaded DB: " + dbPtm.getFullName());
	}	
}