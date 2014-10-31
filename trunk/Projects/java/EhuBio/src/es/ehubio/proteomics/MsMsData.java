package es.ehubio.proteomics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehubio.proteomics.psi.mzid11.AbstractParamType;
import es.ehubio.proteomics.psi.mzid11.AnalysisSoftwareType;
import es.ehubio.proteomics.psi.mzid11.BibliographicReferenceType;
import es.ehubio.proteomics.psi.mzid11.OrganizationType;
import es.ehubio.proteomics.psi.mzid11.ParamListType;
import es.ehubio.proteomics.psi.mzid11.PersonType;
import es.ehubio.proteomics.psi.mzid11.UserParamType;

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
	
	public void loadFromSpectra( Collection<Spectrum> spectra ) {
		psms.clear();
		peptides.clear();
		proteins.clear();
		groups.clear();
		this.spectra.clear();
		this.spectra.addAll(spectra);		
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
	
	public void loadFromPeptides( Set<Peptide> peptides ) {
		spectra.clear();
		psms.clear();
		this.peptides.clear();
		proteins.clear();
		groups.clear();
		for( Peptide peptide : peptides ) {
			Spectrum spectrum = new Spectrum();
			Psm psm = new Psm();
			psm.linkPeptide(peptide);
			psm.linkSpectrum(spectrum);
			spectra.add(spectrum);
		}
		loadFromSpectra(spectra);
	}
	
	public MsMsData markDecoys( String decoyRegex ) {
		if( decoyRegex == null || decoyRegex.isEmpty() )
			return this;
		Pattern pattern = Pattern.compile(decoyRegex);
		for( Protein protein : getProteins() ) {
			Matcher matcher = pattern.matcher(protein.getAccession());
			protein.setDecoy(matcher.find());
		}
		UserParamType param = new UserParamType();
		param.setName("PAnalyzer:Decoy regex");
		param.setValue(decoyRegex);
		setAnalysisParam(param);
		return this;
	}
	
	public void clear() {
		spectra.clear();
		psms.clear();
		peptides.clear();
		proteins.clear();
		groups.clear();
		clearMetaData();
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
		clearMetaData();
		groups.clear();
		spectra.addAll(data2.getSpectra());
		psms.addAll(data2.getPsms());
		mergePeptides(data2.getPeptides());
		mergeProteins(data2.getProteins());
		data2.clear();
	}		
	
	private void mergePeptides(Set<Peptide> peptides2) {
		List<Peptide> listPeptides = new ArrayList<>(peptides);
		peptides.clear();
		listPeptides.addAll(peptides2);		
		Map<String,Peptide> mapPeptide = new HashMap<>();
		for( Peptide peptide : listPeptides ) {
			peptide.getPsms().clear();
			peptide.getScores().clear();
			mapPeptide.put(peptide.getUniqueString(), peptide);
		}		
				
		for( Psm psm : psms ) {
			if( psm.getPeptide() == null )
				continue;
			Peptide peptide = mapPeptide.get(psm.getPeptide().getUniqueString()); 
			psm.linkPeptide(peptide);
			peptides.add(peptide);
		}
	}
	
	private void mergeProteins(Set<Protein> proteins2) {
		List<Protein> listProteins = new ArrayList<>(proteins);
		proteins.clear();
		listProteins.addAll(proteins2);
		Map<String,Protein> mapProtein = new HashMap<>();
		for( Protein protein : listProteins ) {
			protein.getPeptides().clear();
			protein.setGroup(null);
			protein.getScores().clear();
			mapProtein.put(protein.getUniqueString(), protein);
		}
		
		for( Peptide peptide : peptides ) {
			List<Protein> tmp = new ArrayList<>(peptide.getProteins());
			peptide.getProteins().clear();
			for( Protein protein : tmp ) {
				Protein protein2 = mapProtein.get(protein.getUniqueString());
				peptide.addProtein(protein2);
				proteins.add(protein2);
			}
		}
	}
	
	public void checkIntegrity() throws AssertionError {
		for( Peptide peptide : getPeptides() ) {
			if( peptide.getProteins().size() == 0 )
				throw new AssertionError(String.format("Peptide %s not mapped no any protein", peptide.getSequence()));
			for( Protein protein : peptide.getProteins() )
				if( !protein.getPeptides().contains(peptide) )
					throw new AssertionError(String.format("Peptide %s not present in protein %s", peptide.getSequence(), protein.getAccession()));
		}
	}
}