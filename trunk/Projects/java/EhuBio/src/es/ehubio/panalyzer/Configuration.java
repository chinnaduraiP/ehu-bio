package es.ehubio.panalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import es.ehubio.proteomics.ScoreType;

@XmlType(propOrder={
	"description",
	"psmRankThreshold","bestPsmPerPrecursor","psmFdr","psmScore",
	"minPeptideLength","peptideFdr","minPeptideReplicates",
	"proteinFdr","minProteinReplicates","groupFdr",
	"decoyRegex","replicates","filterDecoys","output"})
@XmlRootElement
public class Configuration {	
	public void initialize() {
		setPsmRankThreshold(null);		
		setBestPsmPerPrecursor(true);
		setPsmFdr(null);
		setPsmScore(null);
		setMinPeptideLength(7);
		setPeptideFdr(0.01);
		setMinPeptideReplicates(null);
		setProteinFdr(null);
		setMinProteinReplicates(null);
		setGroupFdr(0.01);
		setDecoyRegex("decoy");
		setFilterDecoys(true);
	}
	
	@XmlAttribute
	public String getVersion() {
		return version;
	}
	
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
	
	@XmlElement(name="replicate")
	public List<Replicate> getReplicates() {
		if( replicates == null )
			replicates = new ArrayList<>();
		return replicates;
	}
	
	public void setReplicates(List<Replicate> replicates) {
		this.replicates = replicates;
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
	
	public Integer getMinPeptideReplicates() {
		return minPeptideReplicates;
	}

	public void setMinPeptideReplicates(Integer minPeptideReplicates) {
		this.minPeptideReplicates = minPeptideReplicates;
	}

	public Integer getMinProteinReplicates() {
		return minProteinReplicates;
	}

	public void setMinProteinReplicates(Integer minProteinReplicates) {
		this.minProteinReplicates = minProteinReplicates;
	}

	public static class Replicate {
		private String name;
		private Set<String> fractions;

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElement(name="fraction")
		public Set<String> getFractions() {
			if( fractions == null )
				fractions = new HashSet<>();
			return fractions;
		}

		public void setFractions(Set<String> fractions) {
			this.fractions = fractions;
		}		
	}

	private final static String version = "1.0";
	private String description;
	private ScoreType psmScore;
	private String decoyRegex;
	private Double psmFdr;
	private Integer psmRankThreshold;
	private Boolean bestPsmPerPrecursor;
	private Integer minPeptideLength;
	private Double peptideFdr;
	private Integer minPeptideReplicates;
	private Double proteinFdr;
	private Integer minProteinReplicates;
	private Double groupFdr;	
	private List<Replicate> replicates;
	private String output;
	private Boolean filterDecoys;
}