package es.ehubio.cosmic;

// See -> http://cancer.sanger.ac.uk/cancergenome/projects/cosmic/download
public class Entry {
	public String getGeneName() {
		return geneName;
	}
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	public String getHgncId() {
		return hgncId;
	}
	public void setHgncId(String hgncId) {
		this.hgncId = hgncId;
	}
	public String getSample() {
		return sample;
	}
	public void setSample(String sample) {
		this.sample = sample;
	}
	public String getPrimarySite() {
		return primarySite;
	}
	public void setPrimarySite(String primarySite) {
		this.primarySite = primarySite;
	}
	public String getSiteSubtype1() {
		return siteSubtype1;
	}
	public void setSiteSubtype1(String siteSubtype1) {
		this.siteSubtype1 = siteSubtype1;
	}
	public String getPrimaryHistology() {
		return primaryHistology;
	}
	public void setPrimaryHistology(String primaryHistology) {
		this.primaryHistology = primaryHistology;
	}
	public String getHistologySubtype1() {
		return histologySubtype1;
	}
	public void setHistologySubtype1(String histologySubtype1) {
		this.histologySubtype1 = histologySubtype1;
	}
	public String getMutationId() {
		return mutationId;
	}
	public void setMutationId(String mutationId) {
		this.mutationId = mutationId;
	}
	public String getMutationCds() {
		return mutationCds;
	}
	public void setMutationCds(String mutationCds) {
		this.mutationCds = mutationCds;
	}
	public String getMutationAa() {
		return mutationAa;
	}
	public void setMutationAa(String mutationAa) {
		this.mutationAa = mutationAa;
	}
	public String getMutationDescription() {
		return mutationDescription;
	}
	public void setMutationDescription(String mutationDescription) {
		this.mutationDescription = mutationDescription;
	}
	public String getMutationZygosity() {
		return mutationZygosity;
	}
	public void setMutationZygosity(String mutationZygosity) {
		this.mutationZygosity = mutationZygosity;
	}
	public String getMutationNcbi36GenomePosition() {
		return mutationNcbi36GenomePosition;
	}
	public void setMutationNcbi36GenomePosition(String mutationNcbi36GenomePosition) {
		this.mutationNcbi36GenomePosition = mutationNcbi36GenomePosition;
	}
	public String getMutationGrch37GenomePosition() {
		return mutationGrch37GenomePosition;
	}
	public void setMutationGrch37GenomePosition(String mutationGrch37GenomePosition) {
		this.mutationGrch37GenomePosition = mutationGrch37GenomePosition;
	}
	public String getPubmedId() {
		return pubmedId;
	}
	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}
	private String geneName;
	private String hgncId;
	private String sample;
	private String primarySite;
	private String siteSubtype1;
	private String primaryHistology;
	private String histologySubtype1;
	private String mutationId;
	private String mutationCds;
	private String mutationAa;
	private String mutationDescription;
	private String mutationZygosity;
	private String mutationNcbi36GenomePosition;
	private String mutationGrch37GenomePosition;
	private String pubmedId;
}
