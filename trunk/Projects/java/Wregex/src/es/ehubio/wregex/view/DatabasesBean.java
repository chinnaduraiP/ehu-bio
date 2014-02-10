package es.ehubio.wregex.view;

import java.io.File;
import java.io.FileInputStream;
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
import es.ehubio.io.UnixCfgReader;
import es.ehubio.wregex.InputGroup;

@ManagedBean
@ApplicationScoped
public class DatabasesBean {
	private static final String WregexMotifsPath = "/resources/data/motifs.xml";
	private static final String ElmMotifsPath = "/resources/data/elm_classes.tsv";
	private static final String TargetsPath = "/resources/data/targets.xml";
	private static final String CosmicPath = "/media/data/Sequences/Cosmic/current/CosmicMutantExportIncFus.tsv.gz";
	
	private MotifConfiguration motifConfiguration;
	private TargetConfiguration targetConfiguration;
	private List<MotifInformation> elmMotifs;
	private Map<String,FastaDb> mapFasta;
	private Map<String,Loci> mapCosmic;
	private long lastModifiedCosmic;
	
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
	
	public String getTest() {
		return "i'm here";
	}

	private void loadDatabases() throws IOException, InvalidSequenceException {
		// Wregex motifs
		Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(WregexMotifsPath)); 		
		motifConfiguration = MotifConfiguration.load(rd);
		rd.close();
		
		// ELM motifs
		loadElmMotifs();
		
		// Targets
		rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(TargetsPath)); 		
		targetConfiguration = TargetConfiguration.load(rd);
		rd.close();
		
		// Fastas
		mapFasta = new HashMap<>();
		for( TargetInformation target : targetConfiguration.getTargets() ) {
			if( !target.getType().equals("fasta") )
				continue;
			FastaDb fasta = new FastaDb();			
			File f = new File(target.getPath());
			fasta.lastModified = f.lastModified();
			fasta.entries = loadFasta(target.getPath());
			mapFasta.put(target.getPath(), fasta);			
		}
		
		// Cosmic
		mapCosmic = Loci.load(CosmicPath);
		lastModifiedCosmic = new File(CosmicPath).lastModified();
	}
	
	private List<InputGroup> loadFasta( String path ) throws IOException, InvalidSequenceException {
		Reader rd;
		if( path.endsWith("gz") )
			rd = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
		else
			rd = new FileReader(path);
		List<InputGroup> result = InputGroup.readEntries(rd);
		rd.close();return result;
	}
	
	public List<MotifInformation> getWregexMotifs() {
		return motifConfiguration.getMotifs();
	}
	
	public List<TargetInformation> getTargets() {
		return targetConfiguration.getTargets();
	}
	
	public List<MotifInformation> getElmMotifs() {
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
	
	public Map<String,Loci> getMapCosmic() {
		File file = new File(CosmicPath);
		if( file.lastModified() != lastModifiedCosmic ) {
			try {
				mapCosmic = Loci.load(CosmicPath);				
			} catch( Exception e ) {
				e.printStackTrace();
			}
			lastModifiedCosmic = file.lastModified();
		}
		return mapCosmic;
	}
	
	private void loadElmMotifs() throws IOException {
		elmMotifs = new ArrayList<>();
		MotifInformation motif;
		MotifDefinition definition;
		List<MotifDefinition> definitions;
		MotifReference reference;
		List<MotifReference> references;
		UnixCfgReader rd = new UnixCfgReader(new InputStreamReader(
				FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(ElmMotifsPath)));
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
	}	
}