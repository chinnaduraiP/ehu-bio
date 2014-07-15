package es.ehubio.proteomics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private Map<String,Peptide> mapPeptide;
	private Map<String,Protein> mapProtein;
	
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
	
	/**
	 * Merge MS/MS data from searches using the same DB (ej. fractions). After the
	 * merging, data2 will be broken and should not be used. Groups will be cleared
	 * and should be re-built next if desired.
	 * 
	 * @param data2
	 */
	public void merge( MsMsData data2 ) {
		spectra.addAll(data2.getSpectra());
		psms.addAll(data2.getPsms());		
		mergeProteins(data2.getProteins());
		mergePeptides(data2.getPeptides());		
		groups.clear();
	}	

	private void mergeProteins(Set<Protein> proteins2) {
		if( mapProtein == null ) {
			mapProtein = new HashMap<>();
			for( Protein protein : proteins )
				mapProtein.put(protein.getUniqueString(), protein);
		}
		for( Protein protein2 : proteins2 ) {
			Protein protein = mapProtein.get(protein2.getUniqueString());
			if( protein == null ) {
				mapProtein.put(protein2.getUniqueString(), protein2);
				proteins.add(protein2);
			} else {
				for( Peptide peptide2 : protein2.getPeptides() ) {
					peptide2.getProteins().remove(protein2);
					peptide2.addProtein(protein);
				}
			}
		}
	}
	
	private void mergePeptides(Set<Peptide> peptides2) {
		if( mapPeptide == null ) {
			mapPeptide = new HashMap<>();
			for( Peptide peptide : peptides )
				mapPeptide.put(peptide.getUniqueString(), peptide);
		}
		for( Peptide peptide2 : peptides2 ) {
			Peptide peptide = mapPeptide.get(peptide2.getUniqueString());
			if( peptide == null ) {
				mapPeptide.put(peptide2.getUniqueString(), peptide2);
				peptides.add(peptide2);
			} else {
				for( Psm psm2 : peptide2.getPsms() )
					psm2.linkPeptide(peptide);
				for( Protein protein2 : peptide2.getProteins() ) {
					protein2.getPeptides().remove(peptide2);
					protein2.addPeptide(peptide);
				}
			}
		}
	}
}