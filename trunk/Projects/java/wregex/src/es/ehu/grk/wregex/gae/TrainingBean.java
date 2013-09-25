package es.ehu.grk.wregex.gae;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.myfaces.custom.fileupload.UploadedFile;

import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.wregex.TrainingEntry;
import es.ehu.grk.wregex.TrainingMotif;

@ManagedBean
@SessionScoped
public class TrainingBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<MotifBean> list = new ArrayList<MotifBean>();
	private UploadedFile uploadedFile;
	
	public TrainingBean() {
	}

	public List<MotifBean> getList() {		
		return list;
	}
	
	public void upload() {
		if( uploadedFile == null )
			return;
		
		this.list.clear();
		try {
			Reader rd = new InputStreamReader(uploadedFile.getInputStream());
			List<TrainingEntry> list = TrainingEntry.readEntries(rd); 
			rd.close();
			for( TrainingEntry p : list ) {
				for( TrainingMotif m : p.getMotifs() )
					this.list.add(new MotifBean(p.getId(),p.getSequence(),m.start+1,m.end+1,m.weight));				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch( InvalidSequenceException e ) {
			e.printStackTrace();
		}
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
	
	public String getSummary() {
		if( list.isEmpty() )
			return null;
		return "Loaded " + list.size() + " training motifs";
	}
}