package es.ehu.grk.wregex.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.wregex.InputGroup;
import es.ehu.grk.wregex.InputMotif;
import es.ehu.grk.wregex.Pssm;
import es.ehu.grk.wregex.PssmBuilder.PssmBuilderException;
import es.ehu.grk.wregex.Trainer;
import es.ehu.grk.wregex.TrainingGroup;
import es.ehu.grk.wregex.TrainingMotif;

@ManagedBean
@SessionScoped
public class TrainingBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<InputMotif> inputList = new ArrayList<>();
	private List<InputGroup> inputGroupList = null;
	private List<TrainingMotif> trainingList = new ArrayList<>();	
	private String regex;
	private String uploadError = null;
	private Trainer trainer = null;
	private String inputFileName = null;
	private int motifsMatched;

	public TrainingBean() {
	}

	public List<InputMotif> getInputList() {		
		return inputList;
	}
	
	public void upload( FileUploadEvent event ) {
		UploadedFile uploadedFile = event.getFile();
		if( uploadedFile == null ) {
			refresh();
			return;
		}
		
		this.inputList.clear();
		try {
			Reader rd = new InputStreamReader(new ByteArrayInputStream(uploadedFile.getContents()));
			inputGroupList = InputGroup.readEntries(rd); 
			rd.close();
			for( InputGroup p : inputGroupList )
				this.inputList.addAll(p.getMotifs());
			uploadError = null;
			inputFileName = uploadedFile.getFileName();
		} catch (IOException e) {
			uploadError = e.getMessage();
			e.printStackTrace();
		} catch( InvalidSequenceException e ) {
			uploadError = e.getMessage();
			e.printStackTrace();
		}
		
		refresh();
	}
	
	public void downloadPssm() {
		if( trainer == null )
			return;
		
		Pssm pssm;
		try {
			pssm = trainer.buildPssm(false);
		} catch (PssmBuilderException e1) {
			e1.printStackTrace();
			return;
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("text/x-fasta"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
	    //ec.setResponseContentLength(length);
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\""+FilenameUtils.removeExtension(inputFileName)+".pssm\"");

		try {
			OutputStream output = ec.getResponseOutputStream();
			pssm.save(new OutputStreamWriter(output),
				"Generated from wregex (v1.0)",
				"Trained with " + getTrainingSummary(),
				"Regex: " + trainer.getRegex(),
				"The following PSSM values are not normalized");
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	    
	    fc.responseComplete();
	}
	
	public void downloadInputMotifs() {
		if( inputGroupList == null )
			return;
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();

	    ec.responseReset();
	    ec.setResponseContentType("text/x-fasta"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
	    //ec.setResponseContentLength(length);
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"motifs.fasta\"");

		try {
			OutputStream output = ec.getResponseOutputStream();
			InputGroup.writeEntries(new OutputStreamWriter(output), inputGroupList);
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	    
	    fc.responseComplete();
	}
	
	public void refresh() {
		motifsMatched = 0;
		trainingList = new ArrayList<>();
		if( inputList.isEmpty() || regex == null || regex.isEmpty() )
			return;
		trainer = new Trainer(regex);
		List<TrainingGroup> groups = trainer.train(inputGroupList,false);
		for( TrainingGroup group : groups )
			trainingList.addAll(group);
		for( InputMotif motif : inputList )
			if( motif.getMatches() != 0 )
				motifsMatched++;
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	public String getInputSummary() {		
		if( inputList.isEmpty() )
			return null;
		if( regex == null )
			return "Loaded " + inputList.size() + " input motifs from " + inputFileName;
		return motifsMatched + " of " + inputList.size() + " input motifs matched";
	}
	
	public String getTrainingSummary() {
		if( trainingList.isEmpty() )			
			return null;
		return getTrainingCount() + " valid matches for " + inputList.size() + " input motifs (" + inputFileName + ")";
	}

	public List<TrainingMotif> getTrainingList() {		
		return trainingList;
	}
	
	public int getTrainingCount(){
		int count = 0;
		for( TrainingMotif motif : trainingList )
			if( motif.isValid() )
				count++;
		return count;
	}
	
	public void remove(TrainingMotif motif) {
		motif.remove();
	}
	
	public void recycle(TrainingMotif motif) {		
		motif.recycle();
	}

	public String getUploadError() {
		return uploadError;
	}
	
	public String getFastaSummary() {
		if( inputGroupList != null && inputFileName != null )
			return inputFileName + ": " + inputGroupList.size() + " entries";
		return null;
	}
}