package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.proteomics.psi.mzid11.AffiliationType;
import es.ehubio.proteomics.psi.mzid11.AnalysisSoftwareType;
import es.ehubio.proteomics.psi.mzid11.BibliographicReferenceType;
import es.ehubio.proteomics.psi.mzid11.CVParamType;
import es.ehubio.proteomics.psi.mzid11.ContactRoleType;
import es.ehubio.proteomics.psi.mzid11.OrganizationType;
import es.ehubio.proteomics.psi.mzid11.ParamType;
import es.ehubio.proteomics.psi.mzid11.PersonType;
import es.ehubio.proteomics.psi.mzid11.RoleType;

public class PAnalyzer {
	private MsMsData data;
	private static final String VERSION = "2.0b1";
	private static final String NAME = "PAnalyzer";
	private static final String URL = "https://code.google.com/p/ehu-bio/wiki/PAnalyzer";
	private static final String CUSTOMIZATIONS = "No customizations";	
	
	public Set<Spectrum> getSpectra() {
		return data.getSpectra();
	}
	
	public Set<Psm> getPsms() {
		return data.getPsms();
	}
	
	public Set<Peptide> getPeptides() {
		return data.getPeptides();
	}
	
	public Set<Protein> getProteins() {
		return data.getProteins();
	}
	
	public Set<ProteinGroup> getGroups() {
		return data.getGroups();
	}
	
	/**
	 * Executes PAnalyzer algorithm.
	 * @see <a href="http://www.biomedcentral.com/1471-2105/13/288">original paper</a>
	 */
	public void run( MsMsData data ) {
		this.data = data;
		data.getGroups().clear();		
		classifyPeptides();
		classifyProteins();
		updateMetadata();
	}		

	private void classifyPeptides() {
		// 1. Locate unique peptides
		for( Peptide peptide : getPeptides() ) {
			if( peptide.getProteins().size() == 1 ) {
				peptide.setConfidence(Peptide.Confidence.UNIQUE);
				peptide.getProteins().iterator().next().setConfidence(Protein.Confidence.CONCLUSIVE);
			} else
				peptide.setConfidence(Peptide.Confidence.DISCRIMINATING);
		}
		
		// 2. Locate non-discriminating peptides (first round)
		for( Protein protein : getProteins() )
			if( protein.getConfidence() == Protein.Confidence.CONCLUSIVE )
				for( Peptide peptide : protein.getPeptides() )
					if( peptide.getConfidence() != Peptide.Confidence.UNIQUE )
						peptide.setConfidence(Peptide.Confidence.NON_DISCRIMINATING);
		
		// 3. Locate non-discriminating peptides (second round)
		for( Peptide peptide : getPeptides() ) {
			if( peptide.getConfidence() != Peptide.Confidence.DISCRIMINATING )
				continue;
			for( Peptide peptide2 : peptide.getProteins().iterator().next().getPeptides() ) {
				if( peptide2.getConfidence() != Peptide.Confidence.DISCRIMINATING )
					continue;
				if( peptide2.getProteins().size() <= peptide.getProteins().size() )
					continue;
				boolean shared = true;
				for( Protein protein : peptide.getProteins() )
					if( !protein.getPeptides().contains(peptide2) ) {
						shared = false;
						break;
					}
				if( shared )
					peptide2.setConfidence(Peptide.Confidence.NON_DISCRIMINATING);
			}
		}
	}
	
	private void classifyProteins() {
		// 1. Locate non-conclusive proteins
		for( Protein protein : getProteins() ) {
			protein.setGroup(null);
			if( protein.getConfidence() == Protein.Confidence.CONCLUSIVE )
				continue;
			protein.setConfidence(Protein.Confidence.NON_CONCLUSIVE);
			for( Peptide peptide : protein.getPeptides() )
				if( peptide.getConfidence() == Peptide.Confidence.DISCRIMINATING ) {
					protein.setConfidence(Protein.Confidence.AMBIGUOUS_GROUP);
					break;
				}			
		}
		
		// 2. Group proteins
		data.getGroups().clear();
		for( Protein protein : getProteins() ) {
			if( protein.getGroup() != null )
				continue;
			ProteinGroup group = new ProteinGroup();
			data.getGroups().add(group);
			buildGroup(group, protein);
		}
		
		// 3. Indistinguishable
		for( ProteinGroup group : data.getGroups() )
			if( group.size() >= 2 )
				if( isIndistinguishable(group) )
					for( Protein protein : group.getProteins() )
						protein.setConfidence(Protein.Confidence.INDISTINGUISABLE_GROUP);
	}
	
	private void buildGroup( ProteinGroup group, Protein protein ) {
		if( group.getProteins().contains(protein) )
			return;
		group.addProtein(protein);
		for( Peptide peptide : protein.getPeptides() ) {
			if( peptide.getConfidence() != Peptide.Confidence.DISCRIMINATING )
				continue;
			for( Protein protein2 : peptide.getProteins() )
				buildGroup(group, protein2);
		}
	}
	
	private boolean isIndistinguishable( ProteinGroup group ) {
		boolean indistinguishable = true;
		Set<Peptide> discrimitating = new HashSet<>();
		for( Protein protein : group.getProteins() )
			for( Peptide peptide : protein.getPeptides() )
				if( peptide.getConfidence() == Peptide.Confidence.DISCRIMINATING )
					discrimitating.add(peptide);			
		for( Protein protein : group.getProteins() )
			if( !protein.getPeptides().containsAll(discrimitating) ) {
				indistinguishable = false;
				break;
			}
		discrimitating.clear();
		return indistinguishable;
	}

	public static String getVersion() {
		return VERSION;
	}

	public static String getName() {
		return NAME;
	}
	
	public static String getFullName() {
		return String.format("%s (v%s)", getName(), getVersion());
	}

	public static String getUrl() {
		return URL;
	}

	public static String getCustomizations() {
		return CUSTOMIZATIONS;
	}
	
	private void updateMetadata() {
		// Organization
		OrganizationType organization = new OrganizationType();
		organization = new OrganizationType();
		organization.setId("UPV/EHU");
		organization.setName("University of the Basque Country (UPV/EHU)");
		data.setOrganization(organization);
		
		// Author
		PersonType author = new PersonType();
		author = new PersonType();
		author.setId("PAnalyzer_Author");
		author.setFirstName("Gorka");
		author.setLastName("Prieto");
		CVParamType email = new CVParamType();
		email.setAccession("MS:1000589");
		email.setName("contact email");
		email.setCvRef("PSI-MS");
		email.setValue("gorka.prieto@ehu.es");
		author.getCvParamsAndUserParams().add(email);
		AffiliationType affiliation = new AffiliationType();
		affiliation.setOrganizationRef(organization.getId());
		author.getAffiliations().add(affiliation);
		data.setAuthor(author);
		
		// Software
		AnalysisSoftwareType software = new AnalysisSoftwareType();
		software.setId(getName());
		software.setName(getName());
		software.setVersion(getVersion());
		software.setUri(getUrl());
		software.setCustomizations(getCustomizations());
		CVParamType cv = new CVParamType();
		cv.setAccession("MS:1002076");
		cv.setName("PAnalyzer");
		cv.setCvRef("PSI-MS");
		ParamType param = new ParamType();
		param.setCvParam(cv);
		software.setSoftwareName(param);
		RoleType role = new RoleType();
		cv = new CVParamType();
		cv.setAccession("MS:1001271");
		cv.setName("researcher");
		cv.setCvRef("PSI-MS");
		role.setCvParam(cv);
		ContactRoleType contact = new ContactRoleType();
		contact.setContactRef(author.getId());
		contact.setRole(role);
		software.setContactRole(contact);
		data.setSoftware(software);
		
		// Reference
		BibliographicReferenceType paper= new BibliographicReferenceType();
		paper.setTitle("PAnalyzer: A software tool for protein inference in shotgun proteomics");
		paper.setName(paper.getTitle());
		paper.setAuthors("Gorka Prieto, Kerman Aloria, Nerea Osinalde, Asier Fullaondo, Jesus M. Arizmendi and Rune Matthiesen");
		paper.setDoi("10.1186/1471-2105-13-288");
		paper.setId(paper.getDoi());
		paper.setVolume("13");
		paper.setIssue("288");
		paper.setYear(2012);
		paper.setPublication("BMC Bioinformatics");
		paper.setPublisher("BioMed Central Ltd.");
		data.setPublication(paper);
	}
}