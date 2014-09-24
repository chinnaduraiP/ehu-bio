package es.ehubio.panalyzer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Configuration {
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String getPsmScore() {
		return psmScore;
	}
	
	public void setPsmScore(String psmScore) {
		this.psmScore = psmScore;
	}
	
	public String getDecoyRegex() {
		return decoyRegex;
	}
	
	public void setDecoyRegex(String decoyRegex) {
		this.decoyRegex = decoyRegex;
	}
	
	public Double getPsmFdr() {
		return psmFdr;
	}
	
	public void setPsmFdr(Double psmFdr) {
		this.psmFdr = psmFdr;
	}
	
	public Double getPeptideFdr() {
		return peptideFdr;
	}
	
	public void setPeptideFdr(Double peptideFdr) {
		this.peptideFdr = peptideFdr;
	}
	
	public Double getGroupFdr() {
		return groupFdr;
	}
	
	public void setGroupFdr(Double groupFdr) {
		this.groupFdr = groupFdr;
	}
	
	@XmlElement(name="input")
	public Set<String> getInputs() {
		if( inputs == null )
			inputs = new HashSet<>();
		return inputs;
	}
	
	public void setInputs(Set<String> inputs) {
		this.inputs = inputs;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	private String description;
	private String operation;
	private String psmScore;
	private String decoyRegex;
	private Double psmFdr;
	private Double peptideFdr;
	private Double groupFdr;	
	private Set<String> inputs;
	private String output;
}