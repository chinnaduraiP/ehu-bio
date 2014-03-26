package es.ehubio.wregex.view;

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
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehubio.cosmic.Loci;
import es.ehubio.cosmic.Locus;
import es.ehubio.db.Aminoacid;
import es.ehubio.db.Fasta.InvalidSequenceException;
import es.ehubio.wregex.InputGroup;
import es.ehubio.wregex.Pssm;
import es.ehubio.wregex.PssmBuilder.PssmBuilderException;
import es.ehubio.wregex.Result;
import es.ehubio.wregex.ResultGroup;
import es.ehubio.wregex.Wregex;
import es.ehubio.wregex.Wregex.WregexException;

@ManagedBean
@SessionScoped
public class SearchBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String motif;
	private String definition;
	private String target;
	private MotifInformation motifInformation;
	private MotifDefinition motifDefinition;
	private DatabaseInformation targetInformation;
	private boolean custom = false;
	private String customRegex;
	private String customPssm;
	private String searchError;
	private List<ResultEx> results = null;
	private boolean usingPssm;
	private boolean grouping = true;
	private boolean cosmic = false;
	private String baseFileName, pssmFileName, fastaFileName;
	private boolean assayScores = false;
	private List<InputGroup> inputGroups = null;
	private Pssm pssm = null;
	@ManagedProperty(value="#{databasesBean}")
	private DatabasesBean databases;
	
	public SearchBean() {	 	
	}
	
	public List<MotifInformation> getWregexMotifs() {
		return databases.getWregexMotifs();
	}
	
	public List<MotifInformation> getElmMotifs() {
		return databases.getElmMotifs();
	}
	
	public List<MotifDefinition> getDefinitions() {
		return motifInformation == null ? null : motifInformation.getDefinitions();
	}
	
	public List<DatabaseInformation> getTargets() {
		return databases.getTargets();
	}
	
	public String getRegex() {
		return motifDefinition == null || motifInformation == null ? null : motifDefinition.getRegex();
	}
	
	public String getPssm() {
		return motifDefinition == null ? null : motifDefinition.getPssm();
	}
	
	public String getSummary() {
		return motifDefinition == null || motifInformation == null ? null : motifInformation.getSummary();
	}
	
	public String getDescription() {
		return motifDefinition == null || motifInformation == null ? null : motifDefinition.getDescription();
	}
	
	public List<MotifReference> getReferences() {
		return motifDefinition == null || motifInformation == null ? null : motifInformation.getReferences();
	}

	public String getMotif() {
		return motif;
	}
	
	public MotifInformation getMotifInformation() {
		return motifInformation;
	}
	
	public DatabaseInformation getTargetInformation() {
		return targetInformation;
	}
	
	public DatabaseInformation getCosmicInformation() {
		return databases.getCosmicInformation();
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
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	private MotifInformation stringToMotif( Object object ) {
		if( object == null )
			return null;
		String name = object.toString();
		for( MotifInformation motif : databases.getWregexMotifs() )
			if( motif.getName().equals(name) )
				return motif;
		for( MotifInformation motif : databases.getElmMotifs() )
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
	
	private DatabaseInformation stringToTarget( Object object ) {
		if( object == null )
			return null;
		String name = object.toString();
		for( DatabaseInformation target : databases.getTargets() )
			if( name.startsWith(target.getName()) )
				return target;
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
	
	public void onChangeTarget( ValueChangeEvent event ) {
		inputGroups = null;
		results = null;
		Object value = event.getNewValue();		
		if( value == null || value.toString().equals("Default") ) {
			targetInformation = null;
			return;
		}
		targetInformation = stringToTarget(event.getNewValue());
		if( targetInformation.getType().equals("fasta") ) {
			try {
				inputGroups = databases.getFasta(targetInformation.getPath());
				fastaFileName = null;
			} catch( Exception e ) {
				searchError = e.getMessage();
			}
		}
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
		if( targetInformation == null )
			return "A target must be selected";
		if( inputGroups == null )
			return "A fasta file with input sequences must be uploaded";
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
			long wdt = System.currentTimeMillis() + Long.parseLong(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("wregex.watchdogtimer"))*1000;
			for( InputGroup inputGroup : inputGroups ) {
				//System.out.println(inputGroup.getHeader());
				if( assayScores )
					resultGroups.addAll(wregex.searchGroupingAssay(inputGroup));
				else
					resultGroups.addAll(wregex.searchGrouping(inputGroup.getFasta()));
				if( System.currentTimeMillis() >= wdt )
					throw new Exception("Too intensive search, try a more strict regular expression or a smaller fasta file");
			}
			results = new ArrayList<>();
			for( ResultGroup resultGroup : resultGroups ) {
				if( grouping )
					results.add(new ResultEx(resultGroup.getRepresentative()));
				else
					for( Result r : resultGroup )
						results.add(new ResultEx(r));
			}
			if( cosmic )
				searchCosmic();
			Collections.sort(results);
		} catch( IOException e ) {
			searchError = "File error: " + e.getMessage();
		} catch( PssmBuilderException e ) {
			searchError = "PSSM not valid: " + e.getMessage();
		} catch( WregexException e ) {
			searchError = "Invalid configuration: " + e.getMessage();
		} catch( Exception e ) {
			searchError = e.getMessage();
		}
	}	

	private void searchCosmic() {
		Map<String,Loci> map = databases.getMapCosmic();
		int missense;
		boolean invalid;
		for( ResultEx result : results ) {
			Loci loci = map.get(result.getGene());
			if( loci == null )
				continue;			
			missense = 0;
			invalid = false;
			for( Locus locus : loci.getLoci().values() ) {
				if( locus.position > result.getFasta().sequence().length() ||
					locus.aa != Aminoacid.parseLetter(result.getFasta().sequence().charAt(locus.position-1)) ) {
					invalid = true;
					break;
				}
				if( locus.position >= result.getStart() && locus.position <= result.getEnd() )
					missense += locus.mutations;
			}
			if( invalid ) {
				result.setCosmicUrl( String.format(
					"http://cancer.sanger.ac.uk/cosmic/gene/analysis?ln=%s&mut=%s",
					result.getGene(), "substitution_missense") );
				continue;
			}
			result.setCosmicUrl( String.format(
				"http://cancer.sanger.ac.uk/cosmic/gene/analysis?ln=%s&start=%d&end=%d&mut=%s",
				result.getGene(), result.getStart(), result.getEnd(), "substitution_missense") );
			result.setCosmicMissense(missense);
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

	public List<ResultEx> getResults() {
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
			ResultEx.saveCsv(new OutputStreamWriter(output), results, assayScores, cosmic );
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
			ResultEx.saveAln(new OutputStreamWriter(output), results);
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
		if( fastaFileName == null )
			return inputGroups.size() + " entries";
		return fastaFileName + ": " + inputGroups.size() + " entries";
	}
	
	public String getPssmSummary() {
		if( custom && pssmFileName != null )
			return pssmFileName;
		return null;
	}
	
	public void onGrouping() {
		searchError = null;
		results = null;
	}
	
	public void onCosmic() {
		searchError = null;
		results = null;
	}
	
	public boolean isUploadTarget() {
		return targetInformation == null ? false : targetInformation.getType().equals("upload");
	}

	public void setDatabases(DatabasesBean databases) {
		this.databases = databases;
	}

	public boolean isCosmic() {
		return cosmic;
	}

	public void setCosmic(boolean cosmic) {
		this.cosmic = cosmic;
	}
}