package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

public class MsMsData {
	private Set<Spectrum> spectra = new HashSet<>();
	private Set<Psm> psms = new HashSet<>();
	private Set<Peptide> peptides = new HashSet<>();
	private Set<Protein> proteins = new HashSet<>();
	
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
	
	public void loadFromSpectra( Set<Spectrum> spectra ) {
		this.spectra = spectra;
		psms.clear();
		peptides.clear();
		proteins.clear();
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
}