package es.ehu.grk.wregex.view;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.io.UnixCfgReader;
import es.ehu.grk.wregex.InputGroup;
import es.ehu.grk.wregex.Pssm;
import es.ehu.grk.wregex.PssmBuilder.PssmBuilderException;
import es.ehu.grk.wregex.Result;
import es.ehu.grk.wregex.ResultGroup;
import es.ehu.grk.wregex.Wregex;
import es.ehu.grk.wregex.Wregex.WregexException;

@ManagedBean
@SessionScoped
public class SearchBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String motif;
	private String definition;
	private MotifConfiguration motifConfiguration;
	private List<MotifInformation> elmMotifs;
	private MotifInformation motifInformation;
	private MotifDefinition motifDefinition;
	private boolean custom = false;
	private String customRegex;
	private String customPssm;
	private String searchError;
	private List<Result> results = null;
	private boolean usingPssm;
	private boolean grouping = true;
	private String baseFileName, pssmFileName, fastaFileName;
	private boolean assayScores = false;
	List<InputGroup> inputGroups = null;
	Pssm pssm = null;
	
	public SearchBean() {		
		try {
			Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/motifs.xml")); 		
			motifConfiguration = MotifConfiguration.load(rd);
			loadElmMotifs();
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<MotifInformation> getMotifs() {
		return motifConfiguration.getMotifs();
	}
	
	public List<MotifDefinition> getDefinitions() {
		return motifInformation == null ? null : motifInformation.getDefinitions();
	}
	
	public String getRegex() {
		return motifDefinition == null || motifInformation == null ? null : motifDefinition.getRegex();
	}
	
	public String getPssm() {
		return motifDefinition == null ? null : motifDefinition.getPssm();
	}
	
	public String getDescription() {
		return motifDefinition == null || motifInformation == null ? null : motifDefinition.getDescription();
	}

	public String getMotif() {
		return motif;
	}
	
	public MotifInformation getMotifInformation() {
		return motifInformation;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public String getConfiguration() {
		return definition;
	}

	public void setConfiguration(String configuration) {
		this.definition = configuration;
	}
	
	private MotifInformation stringToMotif( Object object ) {
		if( object == null )
			return null;
		String name = object.toString();
		for( MotifInformation motif : motifConfiguration.getMotifs() )
			if( motif.getName().equals(name) )
				return motif;
		for( MotifInformation motif : elmMotifs )
			if( motif.getName().equals(name) )
				return motif;
		return null;
	}
	
	private MotifDefinition stringToDefinition( Object object ) {
		if( object == null )
			return null;
		String name = object.toString();
		for( MotifDefinition def : getDefinitions() )
			if( def.getName().equals(name) )
				return def;
		return null;
	}
	
	public void onChangeMotif( ValueChangeEvent event ) {
		Object value = event.getNewValue();
		if( value == null ) {
			custom = false;
			motifInformation = null;
		} else {
			if( value.toString().equals("Custom") ) {
				motifInformation = null;
				custom = true;
			} else {
				motifInformation = (MotifInformation)stringToMotif(event.getNewValue());
				custom = false;
			}
		}
		if( motifInformation == null ) {
			motifDefinition = null;
			setConfiguration("Default");
		} else {
			motifDefinition = motifInformation.getDefinitions().get(0);
			setConfiguration(motifDefinition.toString());
		}
		searchError = null;
		results = null;
		pssm = null;
	}
	
	public void onChangeDefinition( ValueChangeEvent event ) {
		motifDefinition = (MotifDefinition)stringToDefinition(event.getNewValue());
		searchError = null;
		results = null;
		pssm = null;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public String getCustomRegex() {
		return customRegex;
	}

	public void setCustomRegex(String customRegex) {
		this.customRegex = customRegex;
	}

	public String getCustomPssm() {
		return customPssm;
	}

	public void setCustomPssm(String customPssm) {
		this.customPssm = customPssm;
	}

	public String getConfigError() {
		if( custom ) {
			if( customRegex == null || customRegex.isEmpty() )
				return "A regular expression must be defined";
		} else {
			if( motifInformation == null )
				return "A motif must be selected";
			if( motifDefinition == null )
				return "A configuration must be selected for motif " + motif;
		}
		if( inputGroups == null )
			return "A fasta file with input sequences must be selected";
		return null;
	}
	
	public void search() {
		searchError = null;
		try {
			if( !custom )
				loadPssm();
			usingPssm = pssm == null ? false : true;
			String regex = custom ? getCustomRegex() : getRegex();
			Wregex wregex = new Wregex(regex, pssm);
			List<ResultGroup> resultGroups = new ArrayList<>();
			for( InputGroup inputGroup : inputGroups ) {
				if( assayScores )
					resultGroups.addAll(wregex.searchGroupingAssay(inputGroup));
				else
					resultGroups.addAll(wregex.searchGrouping(inputGroup.getFasta()));
			}
			results = new ArrayList<>();
			for( ResultGroup resultGroup : resultGroups ) {
				if( grouping )
					results.add(resultGroup.getRepresentative());
				else
					for( Result r : resultGroup )
						results.add(r);
			}
			Collections.sort(results);
		} catch( IOException e ) {
			searchError = "File error: " + e.getMessage();
		} catch( PssmBuilderException e ) {
			searchError = "PSSM not valid: " + e.getMessage();
		} catch( WregexException e ) {
			searchError = "Invalid configuration: " + e.getMessage();
		}
	}	

	public void uploadPssm( FileUploadEvent event ) {
		searchError = null;
		results = null;
		UploadedFile pssmFile = event.getFile();
		if( !custom || pssmFile == null ) {
			pssm = null;
			return;
		}
		pssmFileName = pssmFile.getFileName();
		//Reader rd = new InputStreamReader(pssmFile.getInputstream());
		Reader rd = new InputStreamReader(new ByteArrayInputStream(pssmFile.getContents()));
		try {
			pssm = Pssm.load(rd, true);
			rd.close();
		} catch (IOException e) {
			searchError = "File error: " + e.getMessage();
		} catch (PssmBuilderException e) {
			searchError = "PSSM not valid: " + e.getMessage();
		}		
	}
	
	private void loadPssm() throws IOException, PssmBuilderException {
		String file = getPssm();
		if( file == null )
			return;
		Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/"+file));
		pssm = Pssm.load(rd, true);
		rd.close();
	}
	
	public void uploadFasta(FileUploadEvent event) {
		searchError = null;
		results = null;
		UploadedFile fastaFile = event.getFile();
		if( fastaFile == null ) {
			inputGroups = null;
			return;
		}
		fastaFileName = fastaFile.getFileName();
		//Reader rd = new InputStreamReader(fastaFile.getInputstream());
		Reader rd = new InputStreamReader(new ByteArrayInputStream(fastaFile.getContents()));
		try {
			inputGroups = InputGroup.readEntries(rd);
			rd.close();
		} catch (IOException e) {
			searchError = "File error: " + e.getMessage();
			return;
		} catch (InvalidSequenceException e) {
			searchError = "Fasta not valid: " + e.getMessage();
			return;
		} 		
		assayScores = true;
		for( InputGroup inputGroup : inputGroups )
			if( !inputGroup.hasScores() ) {
				assayScores = false;
				break;
			}
		baseFileName = FilenameUtils.removeExtension(fastaFile.getFileName());
	}

	public String getSearchError() {
		return searchError;
	}

	public List<Result> getResults() {
		return results;
	}
	
	public String getNumberOfResults() {
		int count = 0;
		if( results != null )
			count = results.size();
		if( count == 0 )
			return "No matches (if a match was expected, try relaxing the regular expression)";
		if( count == 1 )
			return "1 match!!";
		return count + " matches!";
	}

	public boolean isUsingPssm() {
		return usingPssm;
	}

	public boolean isGrouping() {
		return grouping;
	}

	public void setGrouping(boolean grouping) {
		this.grouping = grouping;
	}
	
	public void downloadCsv() {
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("text/csv"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
	    //ec.setResponseContentLength(length);
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\""+baseFileName+".csv\"");
		try {
			OutputStream output = ec.getResponseOutputStream();
			if( assayScores )
				Result.saveAssay(new OutputStreamWriter(output), results, grouping);
			else
				Result.saveCsv(new OutputStreamWriter(output), results);
		} catch( Exception e ) {
			e.printStackTrace();
		}
		fc.responseComplete();
	}
	
	public void downloadAln() {
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("text"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
	    //ec.setResponseContentLength(length);
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\""+baseFileName+".aln\"");
		try {
			OutputStream output = ec.getResponseOutputStream();
			Result.saveAln(new OutputStreamWriter(output), results);
		} catch( Exception e ) {
			e.printStackTrace();
		}
		fc.responseComplete();
	}
	
	public void downloadPssm() {
		String pssmName = getPssm();
		if( pssmName == null )
			return;
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("text"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
	    //ec.setResponseContentLength(length);
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\""+pssmName+"\"");
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(ec.getResourceAsStream("/resources/data/"+pssmName)));
			PrintWriter wr = new PrintWriter(ec.getResponseOutputStream());
			String str;
			while( (str = rd.readLine()) != null )
				wr.println(str);
			rd.close();
			wr.flush();
		} catch( Exception e ) {
			e.printStackTrace();
		}
		fc.responseComplete();
	}		

	public boolean getAssayScores() {
		return assayScores;
	}
	
	public String getFastaSummary() {
		if( inputGroups == null )
			return null;
		return fastaFileName + ": " + inputGroups.size() + " entries";
	}
	
	public String getPssmSummary() {
		if( custom && pssmFileName != null )
			return pssmFileName;
		return null;
	}
	
	public List<MotifInformation> getElmMotifs() {
		return elmMotifs;
	}
	
	void loadElmMotifs() throws IOException {
		elmMotifs = new ArrayList<>();
		MotifInformation motif;
		MotifDefinition definition;
		List<MotifDefinition> definitions;
		MotifReference reference;
		List<MotifReference> references;
		UnixCfgReader rd = new UnixCfgReader(new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/elm_classes.tsv")));
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
			//motif.setSummary(fields[2]);
			definition = new MotifDefinition();
			definition.setName(fields[0]);
			definition.setDescription(fields[2]);
			definition.setRegex(fields[3].replaceAll("\\(", "(?:"));
			definitions = new ArrayList<>();
			definitions.add(definition);
			motif.setDefinitions(definitions);
			reference = new MotifReference();
			reference.setName("ELM");
			reference.setLink("http://elm.eu.org/elms/elmPages/"+fields[1]+".html");
			references = new ArrayList<>();
			references.add(reference);
			motif.setReferences(references);
			elmMotifs.add(motif);
		}
		rd.close();
	}
	
	public void onGrouping() {
		searchError = null;
		results = null;
	}
}