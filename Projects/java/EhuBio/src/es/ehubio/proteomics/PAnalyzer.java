package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

public class PAnalyzer {
	private Set<Spectrum> spectra = new HashSet<>();
	private Set<Psm> psms = new HashSet<>();
	private Set<Peptide> peptides = new HashSet<>();
	private Set<Protein> proteins = new HashSet<>();
	private Set<ProteinGroup> groups = new HashSet<>();
	
	public Set<Spectrum> getSpectra() {
		return spectra;
	}
	
	/**
	 * Feeds PAnalyzer using spectra information.
	 * @see #setPeptides(Set)
	 */
	public void setSpectra(Set<Spectrum> spectra) {
		this.spectra = spectra;
		psms.clear();
		peptides.clear();
		proteins.clear();
		groups.clear();
		for( Spectrum spectrum : spectra ) {
			for( Psm psm : spectrum.getPsms() ) {
				if( psm.getPeptide() == null )
					continue;
				psms.add(psm);
				peptides.add(psm.getPeptide());
				for( Protein protein : psm.getPeptide().getProteins() )
					proteins.add(protein);
			}
		}
	}
	
	public Set<Psm> getPsms() {
		return psms;
	}
	
	public Set<Peptide> getPeptides() {
		return peptides;
	}
	
	/**
	 * Feeds PAnalyzer without considering spectra.
	 * @see #setSpectra(Set)
	 */
	public void setPeptides(Set<Peptide> peptides) {		
		spectra.clear();
		psms.clear();
		this.peptides = peptides;
		proteins.clear();
		for( Peptide peptide : peptides )
			proteins.addAll(peptide.getProteins());
		groups.clear();
	}
	
	public Set<Protein> getProteins() {
		return proteins;
	}
	
	public Set<ProteinGroup> getGroups() {
		return groups;
	}
	
	/**
	 * Executes PAnalyzer algorithm.
	 * @see <a href="http://www.biomedcentral.com/1471-2105/13/288">original paper</a>
	 */
	public Set<ProteinGroup> buildGroups() {
		classifyPeptides();
		classifyProteins();
		return groups;
	}	

	private void classifyPeptides() {
		// 1. Locate unique peptides
		for( Peptide peptide : peptides ) {
			if( peptide.getProteins().size() == 1 ) {
				peptide.setConfidence(Peptide.Confidence.UNIQUE);
				peptide.getProteins().iterator().next().setConfidence(Protein.Confidence.CONCLUSIVE);
			} else
				peptide.setConfidence(Peptide.Confidence.DISCRIMINATING);
		}
		
		// 2. Locate non-discriminating peptides (first round)
		for( Protein protein : proteins )
			if( protein.getConfidence() == Protein.Confidence.CONCLUSIVE )
				for( Peptide peptide : protein.getPeptides() )
					if( peptide.getConfidence() != Peptide.Confidence.UNIQUE )
						peptide.setConfidence(Peptide.Confidence.NON_DISCRIMINATING);
		
		// 3. Locate non-discriminating peptides (second round)
		for( Peptide peptide : peptides ) {
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
		for( Protein protein : proteins ) {
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
		groups.clear();
		for( Protein protein : proteins ) {
			if( protein.getGroup() != null )
				continue;
			ProteinGroup group = new ProteinGroup();
			groups.add(group);
			buildGroup(group, protein);
		}
		
		// 3. Indistinguishable
		for( ProteinGroup group : groups )
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
}