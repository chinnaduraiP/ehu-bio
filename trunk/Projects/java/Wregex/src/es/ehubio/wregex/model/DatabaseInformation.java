package es.ehubio.wregex.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "database")
@XmlAccessorType(XmlAccessType.FIELD)
public class DatabaseInformation implements Serializable {
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlTransient
	public String getFullName() {			
		String v = getVersion();
		if( v == null )
			return getName();
		else
			return getName() + " (" + getVersion() + ")";
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
		if( versionFile == null )
			return version;
		File v = new File(versionFile);
		if( lastModified != v.lastModified() ) {
			reloadVersion();
			lastModified = v.lastModified();
		}
		return version;
	}
	
	private void reloadVersion() {
		version = null;
		try {
			BufferedReader rd;
			rd = new BufferedReader(new FileReader(getVersionFile()));
			version = rd.readLine();
			rd.close();
		} catch(IOException e) {
		}
	}

	public void setVersion(String version) {
		this.version = version;
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
	private long lastModified = -1;
}
