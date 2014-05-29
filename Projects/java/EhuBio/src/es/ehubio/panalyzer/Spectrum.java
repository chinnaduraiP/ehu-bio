package es.ehubio.panalyzer;

import java.util.HashSet;
import java.util.Set;

public final class Spectrum {
	private static int idCount = 1;
	private final int id;
	private String fileName;
	private String fileId;
	private Set<Psm> psms = new HashSet<>();
	
	public Spectrum() {
		id = idCount++;
	}
	
	public int getId() {
		return id;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}		

	public Set<Psm> getPsms() {
		return psms;
	}
	
	public void setPsms(Set<Psm> psms) {
		if( this.psms != null )
			for( Psm psm : this.psms )
				psm.setSpectrum(null);
		this.psms = psms;
		for( Psm psm : psms )
			psm.setSpectrum(this);
	}
	
	@Override
	public String toString() {
		return String.format("%s@%s", getFileId(), getFileName());
	}
	
	@Override
	public boolean equals(Object obj) {
		 if( obj == null )
			 return false;
		 if( !getClass().equals(obj.getClass()) )
			 return false;
		 return ((Spectrum)obj).id == id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
}