package es.ehubio.mymrm.presentation;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.Fasta.SequenceType;
import es.ehubio.mymrm.data.FastaFile;

@ManagedBean
@RequestScoped
public class FastaMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private final FastaFile entity = new FastaFile();
	
	public FastaFile getEntity() {
		return entity;
	}
	
	public void handleFastaUpload( FileUploadEvent event ) {
		try {
			UploadedFile file = event.getFile();
			Reader rd;
			if( file.getFileName().endsWith(".gz") )
				rd = new InputStreamReader(new GZIPInputStream(file.getInputstream()));
			else
				rd = new InputStreamReader(file.getInputstream());
			List<Fasta> entries = Fasta.readEntries(rd, SequenceType.PROTEIN);
			String dir = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("MyMRM.fastaDir");
			Fasta.writeEntries(new File(dir, file.getFileName()).getAbsolutePath(), entries);
			entity.setName(file.getFileName());
		} catch( Exception e ) {			
		}
	}
}
