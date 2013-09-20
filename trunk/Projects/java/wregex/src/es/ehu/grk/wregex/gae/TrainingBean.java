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
import es.ehu.grk.wregex.PssmEntry;

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

	public void setList(List<MotifBean> list) {
		this.list = list;
	}
	
	public void upload() {
		this.list.clear();
		try {
			Reader rd = new InputStreamReader(uploadedFile.getInputStream());
			List<PssmEntry> list = PssmEntry.readEntries(rd); 
			rd.close();
			for( PssmEntry p : list ) {
				for( PssmEntry.Motif m : p.getMotifs() )
					this.list.add(new MotifBean(p.getId(),p.getSequence(),m.start,m.end,m.weight));				
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
}