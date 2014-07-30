package es.ehubio.proteomics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic spectral information in a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class Spectrum {	
	private static int idCount = 1;
	private final int id;
	private String fileName;
	private String fileId;
	private Set<Psm> psms = new HashSet<>();
	private String scan;
	private Double rt;
	private Double intensity;
	private List<FragmentIon> ions = new ArrayList<>();
	
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
	
	@Override
	public String toString() {
		return String.format("%s@%s", getFileId(), getFileName());
	}
	
	public String getUniqueString() {
		return toString();
	}
	
	public String getScan() {
		return scan;
	}

	public void setScan(String scan) {
		this.scan = scan;
	}

	public Double getRt() {
		return rt;
	}

	public void setRt(Double rt) {
		this.rt = rt;
	}

	public List<FragmentIon> getIons() {
		return ions;
	}
	
	public void setIons( List<FragmentIon> ions ) {
		this.ions = ions;
	}

	public Double getIntensity() {
		return intensity;
	}

	public void setIntensity(Double intensity) {
		this.intensity = intensity;
	}
}