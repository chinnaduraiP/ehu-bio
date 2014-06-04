package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;


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
		
		for( Peptide peptide : data.getPeptides() )
			if( peptide.getPsms().isEmpty()
				|| peptide.getSequence().length() < filter.minPeptideLength
				|| (filter.isFilterDecoyPeptides() && peptide.isDecoy()) )
				unlinkPeptide(peptide);
		
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
	}
	
	private static void unlinkPeptide( Peptide peptide ) {
		for( Psm psm : peptide.getPsms() )
			psm.linkPeptide(null);
		for( Protein protein : peptide.getProteins() )
			protein.getPeptides().remove(peptide);
	}
	
	private static void unlinkPsm( Psm psm ) {
		if( psm.getSpectrum() != null )
			psm.getSpectrum().getPsms().remove(psm);
		if( psm.getPeptide() != null )
			psm.getPeptide().getPsms().remove(psm);
	}
}