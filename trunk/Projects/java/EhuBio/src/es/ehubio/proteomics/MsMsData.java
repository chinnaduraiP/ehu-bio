package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ehubio.proteomics.psi.mzid11.AbstractParamType;
import es.ehubio.proteomics.psi.mzid11.AnalysisSoftwareType;
import es.ehubio.proteomics.psi.mzid11.BibliographicReferenceType;
import es.ehubio.proteomics.psi.mzid11.OrganizationType;
import es.ehubio.proteomics.psi.mzid11.ParamListType;
import es.ehubio.proteomics.psi.mzid11.PersonType;

/**
 * Mutable class for storing and processing data associated with
 * a MS/MS proteomics experiment.
 * 
 * @author gorka
 *
 */
public class MsMsData {
	private Set<Spectrum> spectra = new HashSet<>();
	private Set<Psm> psms = new HashSet<>();
	private Set<Peptide> peptides = new HashSet<>();
	private Set<Protein> proteins = new HashSet<>();
	private Set<ProteinGroup> groups = new HashSet<>();
	
	private OrganizationType organization;
	private PersonType author;
	private AnalysisSoftwareType software;
	private BibliographicReferenceType publication;
	private ParamListType analysisParams;
	private ParamListType thresholds;

	public Set<Spectrum> getSpectra() {
		return spectra;
	}
	
	public Set<Psm> getPsms() {
		return psms;
	}
	
	public Set<Peptide> getPeptides() {
		return peptides;
	}
	
	public Set<Protein> getProteins() {
		return proteins;
	}
	
	public Set<ProteinGroup> getGroups() {
		return groups;
	}
	
	public void loadFromSpectra( Set<Spectrum> spectra ) {
		this.spectra = spectra;
		psms.clear();
		peptides.clear();
		proteins.clear();
		groups.clear();
		for( Spectrum spectrum : spectra )			
			for( Psm psm : spectrum.getPsms() ) {				
				psms.add(psm);
				if( psm.getPeptide() == null )
					continue;
				peptides.add(psm.getPeptide());
				for( Protein protein : psm.getPeptide().getProteins() ) {
					proteins.add(protein);
					if( protein.getGroup() != null )
						groups.add(protein.getGroup());
				}
			}
	}
	
	public void clearMetaData() {
		organization = null;
		author = null;
		software = null;
		publication = null;
		analysisParams = null;
		thresholds = null;
	}
	
	public OrganizationType getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationType organization) {
		this.organization = organization;
	}

	public PersonType getAuthor() {
		return author;
	}

	public void setAuthor(PersonType author) {
		this.author = author;
	}

	public AnalysisSoftwareType getSoftware() {
		return software;
	}

	public void setSoftware(AnalysisSoftwareType software) {
		this.software = software;
	}

	public BibliographicReferenceType getPublication() {
		return publication;
	}

	public void setPublication(BibliographicReferenceType publication) {
		this.publication = publication;
	}
	
	public void setAnalysisParam(AbstractParamType param) {
		if( analysisParams == null )
			analysisParams = new ParamListType();
		setParam(analysisParams.getCvParamsAndUserParams(), param);
	}
	
	public ParamListType getAnalysisParams() {
		return analysisParams;
	}
	
	public void setThreshold(AbstractParamType param) {
		if( thresholds == null )
			thresholds = new ParamListType();
		setParam(thresholds.getCvParamsAndUserParams(), param);
	}
	
	public ParamListType getThresholds() {
		return thresholds;
	}
	
	@Override
	public String toString() {
		return String.format("%d groups, %d proteins, %d peptides, %d psms, %d spectra",
			getGroups().size(), getProteins().size(), getPeptides().size(), getPsms().size(), getSpectra().size());
	}
	
	private void setParam(List<AbstractParamType> list, AbstractParamType param) {
		AbstractParamType remove = null;
		for( AbstractParamType item : list )
			if( item.getName().equals(param.getName()) ) {
				remove = item;
				break;
			}
		if( remove != null )
			list.remove(remove);
		list.add(param);
	}
}