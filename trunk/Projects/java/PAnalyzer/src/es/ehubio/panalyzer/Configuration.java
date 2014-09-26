package es.ehubio.panalyzer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import es.ehubio.proteomics.ScoreType;

@XmlType(propOrder={
	"description",
	"psmRankThreshold","bestPsmPerPrecursor","psmFdr","psmScore",
	"minPeptideLength","peptideFdr",
	"proteinFdr","groupFdr",
	"decoyRegex","inputs","filterDecoys","output"})
@XmlRootElement
public class Configuration {	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ScoreType getPsmScore() {
		return psmScore;
	}
	
	public void setPsmScore(ScoreType psmScore) {
		this.psmScore = psmScore;
	}
	
	public String getDecoyRegex() {
		return decoyRegex;
	}
	
	public void setDecoyRegex(String decoyRegex) {
		this.decoyRegex = decoyRegex;
	}	
	
	public Integer getPsmRankThreshold() {
		return psmRankThreshold;
	}

	public void setPsmRankThreshold(Integer psmRankThreshold) {
		this.psmRankThreshold = psmRankThreshold;
	}

	public Boolean getBestPsmPerPrecursor() {
		return bestPsmPerPrecursor;
	}

	public void setBestPsmPerPrecursor(Boolean bestPsmPerPrecursor) {
		this.bestPsmPerPrecursor = bestPsmPerPrecursor;
	}
	
	public Double getPsmFdr() {
		return psmFdr;
	}
	
	public void setPsmFdr(Double psmFdr) {
		this.psmFdr = psmFdr;
	}
	
	public Integer getMinPeptideLength() {
		return minPeptideLength;
	}

	public void setMinPeptideLength(Integer minPeptideLength) {
		this.minPeptideLength = minPeptideLength;
	}
	
	public Double getProteinFdr() {
		return proteinFdr;
	}

	public void setProteinFdr(Double proteinFdr) {
		this.proteinFdr = proteinFdr;
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
	
	public Boolean getFilterDecoys() {
		return filterDecoys;
	}

	public void setFilterDecoys(Boolean filterDecoys) {
		this.filterDecoys = filterDecoys;
	}

	private String description;
	private ScoreType psmScore;
	private String decoyRegex;
	private Double psmFdr;
	private Integer psmRankThreshold;
	private Boolean bestPsmPerPrecursor;
	private Integer minPeptideLength;
	private Double peptideFdr;
	private Double proteinFdr;
	private Double groupFdr;	
	private Set<String> inputs;
	private String output;
	private Boolean filterDecoys;
}