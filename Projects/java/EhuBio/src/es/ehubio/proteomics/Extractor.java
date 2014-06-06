package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

import es.ehubio.proteomics.psi.mzid11.UserParamType;


public final class Extractor {
	public static class Filter {
		private Psm.ScoreType psmScoreType;
		private Double psmScoreThreshold;
		private boolean psmLargerScoreBetter = true;
		private boolean mzidPassThreshold = true;
		private int minPeptideLength = 0;
		private boolean filterDecoyPeptides = true;		
		
		public Psm.ScoreType getPsmScoreType() {
			return psmScoreType;
		}
		
		public Double getPsmScoreThreshold() {
			return psmScoreThreshold;
		}
		
		public boolean isPsmLargerScoreBetter() {
			return psmLargerScoreBetter;
		}
		
		public void setPsmScore( Psm.ScoreType psmScoreType, double psmScoreThreshold, boolean psmLargerScoreBetter) {
			this.psmScoreType = psmScoreType;
			this.psmScoreThreshold = psmScoreThreshold;
			this.psmLargerScoreBetter = psmLargerScoreBetter;
		}
		
		public int getMinPeptideLength() {
			return minPeptideLength;
		}
		
		public void setMinPeptideLength(int minPeptideLength) {
			this.minPeptideLength = minPeptideLength;
		}

		public boolean isFilterDecoyPeptides() {
			return filterDecoyPeptides;
		}

		public void setFilterDecoyPeptides(boolean filterDecoyPeptides) {
			this.filterDecoyPeptides = filterDecoyPeptides;
		}

		public boolean isMzidPassThreshold() {
			return mzidPassThreshold;
		}

		public void setMzidPassThreshold(boolean mzidPassThreshold) {
			this.mzidPassThreshold = mzidPassThreshold;
		}
	}
	
	private MsMsData data;

	public void setData(MsMsData data) {
		this.data = data;
	}
	
	public void filterData( Filter filter ) {
		if( data == null || filter == null )
			return;

		// Filter peptides
		for( Peptide peptide : data.getPeptides() )
			if( peptide.getPsms().isEmpty()
				|| peptide.getSequence().length() < filter.minPeptideLength
				|| (filter.isFilterDecoyPeptides() && peptide.isDecoy()) )
				unlinkPeptide(peptide);
		
		// Filter psms
		for( Psm psm : data.getPsms() ) {
			if( psm.getPeptide() == null ) {
				unlinkPsm(psm);
				continue;
			}
			if( filter.isMzidPassThreshold() ) {
				Double score = psm.getScoreByType(Psm.ScoreType.MZID_PASS_THRESHOLD);
				if( score != null && score < 0.5 ) {
					unlinkPsm(psm);
					continue;
				}
			}
			if( filter.getPsmScoreType() == null || filter.getPsmScoreThreshold() == null)
				continue;
			Double score = psm.getScoreByType(filter.getPsmScoreType());
			if( score == null ||
				(filter.isPsmLargerScoreBetter() && score < filter.getPsmScoreThreshold()) ||
				(!filter.isPsmLargerScoreBetter() && score > filter.getPsmScoreThreshold()) ) {
				unlinkPsm(psm);
			}
		}
		
		Set<Spectrum> spectra = new HashSet<>();
		for( Spectrum spectrum : data.getSpectra() )
			if( !spectrum.getPsms().isEmpty() )
				spectra.add(spectrum);
		data.loadFromSpectra(spectra);
		
		updateMetaData( filter );
	}
	
	private void updateMetaData( Filter filter ) {
		UserParamType param = null;
		
		if( filter.getMinPeptideLength() > 0 ) {
			param = new UserParamType();
			param.setName("EhuBio:Minimum peptide length");
			param.setValue(""+filter.getMinPeptideLength());
			data.addAnalysisParam(param);
		}
		if( filter.getPsmScoreThreshold() != null ) {
			param = new UserParamType();
			param.setName("EhuBio:PSM score type");
			param.setValue(filter.getPsmScoreType().getName());
			data.addAnalysisParam(param);
			param = new UserParamType();
			param.setName("EhuBio:PSM score threshold");
			param.setValue(filter.getPsmScoreThreshold().toString());
			data.addAnalysisParam(param);
		}
	}

	private static void unlinkProtein(Protein protein) {
		Set<Peptide> peptides = new HashSet<>();
		peptides.addAll(protein.getPeptides());
		protein.getPeptides().clear();
		for( Peptide peptide : peptides ) {
			peptide.getProteins().remove(protein);
			if( peptide.getProteins().isEmpty() )
				unlinkPeptide(peptide);
		}
		
		if( protein.getGroup() != null ) {
			protein.getGroup().getProteins().remove(protein);
			protein.setGroup(null);
		}
	}
	
	private static void unlinkPeptide( Peptide peptide ) {
		Set<Psm> psms = new HashSet<>();
		psms.addAll(peptide.getPsms());
		peptide.getPsms().clear();
		for( Psm psm : psms )
			unlinkPsm(psm);		
		
		Set<Protein> proteins = new HashSet<>();
		proteins.addAll(peptide.getProteins());
		peptide.getProteins().clear();
		for( Protein protein : proteins ) {
			protein.getPeptides().remove(peptide);
			if( protein.getPeptides().isEmpty() )
				unlinkProtein(protein);
		}		
	}	

	private static void unlinkPsm( Psm psm ) {
		Spectrum spectrum = psm.getSpectrum(); 
		if( spectrum != null ) {
			psm.linkSpectrum(null);
			spectrum.getPsms().remove(psm);
		}
		Peptide peptide = psm.getPeptide(); 
		if( peptide != null ) {
			psm.linkPeptide(null);
			peptide.getPsms().remove(psm);
			if( peptide.getPsms().isEmpty() )
				unlinkPeptide(peptide);
		}
	}
}