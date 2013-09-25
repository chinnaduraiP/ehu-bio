package es.ehu.grk.wregex.gae;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.myfaces.custom.fileupload.UploadedFile;

import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.wregex.InputMotif;
import es.ehu.grk.wregex.Trainer;
import es.ehu.grk.wregex.TrainingEntry;
import es.ehu.grk.wregex.TrainingGroup;
import es.ehu.grk.wregex.TrainingMotif;

@ManagedBean
@SessionScoped
public class TrainingBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<InputMotif> inputList = new ArrayList<>();
	private List<TrainingMotif> trainingList = new ArrayList<>();
	private DataModel<TrainingMotif> trainingModel;	
	private UploadedFile uploadedFile;
	private String regex;

	public TrainingBean() {
	}

	public List<InputMotif> getInputList() {		
		return inputList;
	}
	
	public void upload() {
		if( uploadedFile == null )
			return;
		
		this.inputList.clear();
		try {
			Reader rd = new InputStreamReader(uploadedFile.getInputStream());
			List<TrainingEntry> list = TrainingEntry.readEntries(rd); 
			rd.close();
			for( TrainingEntry p : list )
				this.inputList.addAll(p.getMotifs());		
		} catch (IOException e) {
			e.printStackTrace();
		} catch( InvalidSequenceException e ) {
			e.printStackTrace();
		}
		
		refresh();
	}
	
	public void refresh() {
		trainingList = new ArrayList<>();
		trainingModel = new ListDataModel<>(trainingList);
		if( inputList.isEmpty() || regex == null || regex.isEmpty() )
			return;
		Trainer trainer = new Trainer(regex);
		List<TrainingGroup> groups = trainer.train(inputList);
		for( TrainingGroup group : groups )
			trainingList.addAll(group);
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
	
	public String getInputSummary() {
		if( inputList.isEmpty() )
			return null;		
		return "Loaded " + inputList.size() + " input motifs";		
	}
	
	public String getTrainingSummary() {
		if( trainingList.isEmpty() )			
			return null;
		return trainingList.size() + " matches for " + inputList.size() + " input motifs";
	}

	public List<TrainingMotif> getTrainingList() {		
		return trainingList;
	}
	
	public DataModel<TrainingMotif> getTrainingModel() {
		return trainingModel;
	}

	public void setTrainingModel(DataModel<TrainingMotif> trainingModel) {
		this.trainingModel = trainingModel;
	}
	
	public void remove(TrainingMotif motif) {
		motif.remove();
	}
	
	public void recycle(TrainingMotif motif) {		
		motif.recycle();
	}
}