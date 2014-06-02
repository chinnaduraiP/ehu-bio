package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.Util;

public class Spectrum {
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
	
	public boolean addPsm( Psm psm ) {
		if( psm.getSpectrum() != this )
			psm.linkSpectrum(this);
		return psms.add(psm);
	}
	
	public boolean removePsm( Psm psm ) {
		psm.linkSpectrum(null);
		return psms.remove(psm);
	}
	
	@Override
	public String toString() {
		return String.format("%s@%s", getFileId(), getFileName());
	}
	
	@Override
	public boolean equals(Object obj) {
		 if( obj == null )
			 return false;
		 if( !getClass().isInstance(obj) )
			 return false;
		 Spectrum spectrum = (Spectrum)obj;
		 if( Util.compare(getFileName(), spectrum.getFileName()) && Util.compare(getFileId(), spectrum.getFileId()) )
			 return true;		 
		 return false;
	}
	
	@Override
	public int hashCode() {
		return Util.hashCode(fileName, fileId);
	}
}