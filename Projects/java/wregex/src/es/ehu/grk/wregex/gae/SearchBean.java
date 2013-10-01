package es.ehu.grk.wregex.gae;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import org.apache.myfaces.custom.fileupload.UploadedFile;

import es.ehu.grk.db.Fasta;
import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.db.Fasta.SequenceType;
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
	private MotifInformation motifInformation;
	private MotifDefinition motifDefinition;
	private boolean custom = false;
	private String customRegex;
	private String customPssm;
	private UploadedFile pssmFile;
	private UploadedFile fastaFile;
	private String searchError;
	private List<Result> results = null;
	private boolean usingPssm;
	private boolean grouping = true;
	private String baseFileName;
	
	public SearchBean() {
		Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/motifs.xml")); 		
		motifConfiguration = MotifConfiguration.load(rd);
		try {
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
		return motifDefinition == null ? null : motifDefinition.getRegex();
	}
	
	public String getPssm() {
		return motifDefinition == null ? null : motifDefinition.getPssm();
	}
	
	public String getDescription() {
		return motifDefinition == null ? null : motifDefinition.getDescription();
	}

	public String getMotif() {
		return motif;
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
	
	private MotifInformation stringToMotif( String name ) {
		if( name == null )
			return null;
		for( MotifInformation motif : motifConfiguration.getMotifs() )
			if( motif.getName().equals(name) )
				return motif;
		return null;
	}
	
	private MotifDefinition stringToDefinition( String name ) {
		if( name == null )
			return null;
		for( MotifDefinition def : getDefinitions() )
			if( def.getName().equals(name) )
				return def;
		return null;
	}
	
	public void onChangeMotif( ValueChangeEvent event ) {
		if( event.getNewValue().toString().equals("Custom") ) {
			motifInformation = null;
			custom = true;
		} else {
			motifInformation = (MotifInformation)stringToMotif(event.getNewValue().toString());
			custom = false;
		}
		motifDefinition = null;
		searchError = null;
		results = null;
	}
	
	public void onChangeDefinition( ValueChangeEvent event ) {
		motifDefinition = (MotifDefinition)stringToDefinition(event.getNewValue().toString());
		searchError = null;
		results = null;
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
		return null;
	}

	public UploadedFile getPssmFile() {
		return pssmFile;
	}

	public void setPssmFile(UploadedFile pssmFile) {
		this.pssmFile = pssmFile;
	}

	public UploadedFile getFastaFile() {
		return fastaFile;
	}

	public void setFastaFile(UploadedFile fastaFile) {
		this.fastaFile = fastaFile;
	}
	
	public void search() {
		if( fastaFile == null ) {
			searchError = "A fasta file with input sequences must be selected";
			results = null;
			return;
		}
		searchError = null;
		try {
			Pssm pssm = uploadPssm();
			usingPssm = pssm == null ? false : true;
			List<Fasta> fastas = uploadFasta();
			String regex = custom ? getCustomRegex() : getRegex();
			Wregex wregex = new Wregex(regex, pssm);
			if( !grouping )
				results = wregex.search(fastas);
			else {
				results = new ArrayList<>();
				for( ResultGroup group : wregex.searchGrouping(fastas) )
					results.add(group.getRespresentative());
			}
			Collections.sort(results);
		} catch( IOException e ) {
			searchError = "File error: " + e.getMessage();
		} catch( PssmBuilderException e ) {
			searchError = "PSSM not valid: " + e.getMessage();
		} catch( InvalidSequenceException e ) {
			searchError = "Fasta not valid: " + e.getMessage();
		} catch( WregexException e ) {
			searchError = "Invalid configuration: " + e.getMessage();
		}
	}	

	private Pssm uploadPssm() throws IOException, PssmBuilderException {
		if( custom && pssmFile == null )
			return null;
		if( !custom && getPssm() == null )
			return null;
		Reader rd;
		if( custom ) 
			rd = new InputStreamReader(pssmFile.getInputStream());
		else
			rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/"+getPssm()));
		Pssm pssm = Pssm.load(rd, true);
		rd.close();
		return pssm;
	}
	
	private List<Fasta> uploadFasta() throws IOException, InvalidSequenceException {
		Reader rd = new InputStreamReader(fastaFile.getInputStream());
		List<Fasta> fastas = Fasta.readEntries(rd, SequenceType.PROTEIN);
		rd.close();
		baseFileName = FilenameUtils.removeExtension(fastaFile.getName());
		return fastas;
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
}
