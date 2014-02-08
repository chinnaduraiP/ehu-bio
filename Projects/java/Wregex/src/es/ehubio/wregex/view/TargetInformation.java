package es.ehubio.wregex.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "target")
@XmlAccessorType(XmlAccessType.FIELD)
public class TargetInformation implements Serializable {
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		fullName = null;
	}
	
	@XmlTransient
	public String getFullName() {
		if( fullName == null ) {			
			String v = getVersion();
			if( v == null )
				fullName = getName();
			else
				fullName = getName() + " (" + getVersion() + ")";
		}			
		return fullName;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getVersionFile() {
		return versionFile;
	}
	
	public void setVersionFile(String versionFile) {
		this.versionFile = versionFile;
		version = null;
	}		
	
	public String getVersion() {
		if( version == null && versionFile != null ) {			
			try {
				BufferedReader rd;
				rd = new BufferedReader(new FileReader(getVersionFile()));
				version = rd.readLine();
				rd.close();
			} catch(IOException e) {
			}			
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		fullName = null;
	}
	
	@Override
	public String toString() {
		return getFullName();
	}

	private static final long serialVersionUID = 1L;
	private String name;
	private String type;
	private String path;
	private String version;
	private String versionFile;
	private String fullName;
}
