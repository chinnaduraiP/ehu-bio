package es.ehu.grk.wregex.gae;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

@ManagedBean
@SessionScoped
public class SearchBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String motif;
	private String definition;
	private MotifConfiguration motifConfiguration;
	private MotifInformation motifInformation;
	private MotifDefinition motifDefinition;
	
	public SearchBean() {
		Reader rd = new InputStreamReader(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/data/motifs.xml")); 		
		motifConfiguration = MotifConfiguration.load(rd);
		try {
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<MotifInformation> getMotifs() {
		return motifConfiguration.getMotifs();
	}
	
	public List<MotifDefinition> getDefinitions() {
		return motifInformation == null ? null : motifInformation.getDefinitions();
	}
	
	public String getRegex() {
		return motifDefinition == null ? null : motifDefinition.getRegex();
	}
	
	public String getPssm() {
		return motifDefinition == null ? null : motifDefinition.getPssm();
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public String getConfiguration() {
		return definition;
	}

	public void setConfiguration(String configuration) {
		this.definition = configuration;
	}
	
	private MotifInformation stringToMotif( String name ) {
		if( name == null )
			return null;
		for( MotifInformation motif : motifConfiguration.getMotifs() )
			if( motif.getName().equals(name) )
				return motif;
		return null;
	}
	
	private MotifDefinition stringToDefinition( String name ) {
		if( name == null )
			return null;
		for( MotifDefinition def : getDefinitions() )
			if( def.getName().equals(name) )
				return def;
		return null;
	}
	
	public void onChangeMotif( ValueChangeEvent event ) {
		motifInformation = (MotifInformation)stringToMotif(event.getNewValue().toString());
		motifDefinition = null;
	}
	
	public void onChangeDefinition( ValueChangeEvent event ) {
		motifDefinition = (MotifDefinition)stringToDefinition(event.getNewValue().toString());
	}
}
