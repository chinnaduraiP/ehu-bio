package es.ehubio.proteomics.pipeline;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.Spectrum;
import es.ehubio.proteomics.psi.mzid11.CVParamType;
import es.ehubio.proteomics.psi.mzid11.UserParamType;

public class Filter {
	private final static Logger logger = Logger.getLogger(Filter.class.getName());
	private Score psmScoreThreshold;
	private Score peptideScoreThreshold;
	private Score proteinScoreThreshold;
	private Score groupScoreThreshold;
	private boolean mzidPassThreshold = false;
	private int minPeptideLength = 0;
	private boolean filterDecoyPeptides = false;
	private int rankTreshold = 0;
	private Double ppmThreshold;
	private final MsMsData data;
	
	public Filter( MsMsData data ) {
		this.data = data;
	}
	
	public Score getPsmScoreThreshold() {
		return psmScoreThreshold;
	}
	
	public void setPsmScoreThreshold( Score psmScoreThreshold ) {
		this.psmScoreThreshold = psmScoreThreshold;
	}
	
	public Score getPeptideScoreThreshold() {
		return peptideScoreThreshold;
	}

	public void setPeptideScoreThreshold(Score peptideScoreThreshold) {
		this.peptideScoreThreshold = peptideScoreThreshold;
	}

	public Score getProteinScoreThreshold() {
		return proteinScoreThreshold;
	}

	public void setProteinScoreThreshold(Score proteinScoreThreshold) {
		this.proteinScoreThreshold = proteinScoreThreshold;
	}
	
	public Score getGroupScoreThreshold() {
		return groupScoreThreshold;
	}

	public void setGroupScoreThreshold(Score groupScoreThreshold) {
		this.groupScoreThreshold = groupScoreThreshold;
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
	
	public int getRankTreshold() {
		return rankTreshold;
	}

	public void setRankTreshold(int rankTreshold) {
		this.rankTreshold = rankTreshold;
	}
	
	public Double getPpmThreshold() {
		return ppmThreshold;
	}

	public void setPpmThreshold(Double ppmThreshold) {
		this.ppmThreshold = ppmThreshold;
	}
	
	public void run() {
		filterGroups();
		filterProteins();
		filterPeptides();		
		filterPsms();
		
		Set<Spectrum> spectra = new HashSet<>();
		for( Spectrum spectrum : data.getSpectra() )
			if( !spectrum.getPsms().isEmpty() )
				spectra.add(spectrum);
		data.loadFromSpectra(spectra);
		
		updateMetaData();
	}
	
	public double runGroupFdrThreshold( ScoreType type, double fdr) {		
		PAnalyzer pAnalyzer = new PAnalyzer(data);				
		pAnalyzer.run();
		run();
		pAnalyzer.run();
		
		Validator validator = new Validator(data);
		double prev = validator.getGroupFdrThreshold(type, fdr);
		double tmp = prev;
		
		Score score = new Score(type, prev);
		setGroupScoreThreshold(score);
		int i = 1;
		do {
			prev = tmp;
			run();
			pAnalyzer.run();
			tmp = validator.getGroupFdrThreshold(type, fdr);
			score.setValue(tmp);
			logger.info(String.format("Iteration: %s -> prev=%s, new=%s", i++, prev, tmp));
		} while( tmp != prev );
		
		return prev;
	}
	
	private void filterPsms() {
		for( Psm psm : data.getPsms() ) {
			if( psm.getPeptide() == null ) {
				unlinkPsm(psm);
				continue;
			}
			if( getRankTreshold() > 0 && (psm.getRank() == null || psm.getRank() > getRankTreshold()) ) {
				unlinkPsm(psm);
				continue;
			}
			if( getPpmThreshold() != null && (psm.getPpm() == null || psm.getPpm() > getPpmThreshold()) ) {
				unlinkPsm(psm);
				continue;
			}
			if( isMzidPassThreshold() ) {
				Score score = psm.getScoreByType(ScoreType.MZID_PASS_THRESHOLD);
				if( score != null && score.getValue() < 0.5 ) {
					unlinkPsm(psm);
					continue;
				}
			}
			if( getPsmScoreThreshold() == null )
				continue;
			Score score = psm.getScoreByType(getPsmScoreThreshold().getType());
			if( score == null || getPsmScoreThreshold().compare(score.getValue()) > 0 )
				unlinkPsm(psm);
		}
	}

	private void filterPeptides() {
		for( Peptide peptide : data.getPeptides() ) {
			if( peptide.getPsms().isEmpty() ) {
				unlinkPeptide(peptide);
				continue;
			}
			if( peptide.getSequence().length() < getMinPeptideLength() ) {
				unlinkPeptide(peptide);
				continue;
			}
			if( isFilterDecoyPeptides() && Boolean.TRUE.equals(peptide.getDecoy()) ) {
				unlinkPeptide(peptide);
				continue;
			}
			if( getPeptideScoreThreshold() == null )
				continue;
			Score score = peptide.getScoreByType(getPeptideScoreThreshold().getType());
			if( score == null || getPeptideScoreThreshold().compare(score.getValue()) > 0 )
				unlinkPeptide(peptide);
		}				
	}

	private void filterProteins() {
		if( getProteinScoreThreshold() != null )
			for( Protein protein : data.getProteins() ) {
				Score score = protein.getScoreByType(getProteinScoreThreshold().getType());
				if( score == null || getProteinScoreThreshold().compare(score.getValue()) > 0 )
					unlinkProtein(protein);
			}
	}

	private void filterGroups() {
		if( getGroupScoreThreshold() != null )
			for( ProteinGroup group : data.getGroups() ) {
				Score score = group.getScoreByType(getGroupScoreThreshold().getType());
				if( score == null || getGroupScoreThreshold().compare(score.getValue()) > 0 )
					unlinkGroup(group);
			}
	}

	private void updateMetaData() {
		updatePsmMetaData();
		updatePeptideMetaData();
		updateProteinMetaData();
		updateGroupMetaData();

		CVParamType cvParam = new CVParamType();
		cvParam.setAccession("MS:1001194");
		cvParam.setCvRef("PSI-MS");
		cvParam.setName("quality estimation with decoy database");
		cvParam.setValue(""+isFilterDecoyPeptides());
		data.addAnalysisParam(cvParam);
	}

	private void updatePsmMetaData() {
		UserParamType userParam = null;
		
		if( getPsmScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:PSM score type");
			userParam.setValue(getPsmScoreThreshold().getName());
			data.addAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("EhuBio:PSM score threshold");
			userParam.setValue(getPsmScoreThreshold().getValue()+"");
			data.addAnalysisParam(userParam);
		}
		if( getRankTreshold() > 0 ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:PSM rank threshold");
			userParam.setValue(getRankTreshold()+"");
			data.addAnalysisParam(userParam);
		}
		if( getPpmThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:PSM ppm threshold");
			userParam.setValue(getPpmThreshold()+"");
			data.addAnalysisParam(userParam);
		}
		
		userParam = new UserParamType();
		userParam.setName("EhuBio:Using search engine PSM threshold");
		userParam.setValue(isMzidPassThreshold()+"");
		data.addAnalysisParam(userParam);
	}
	
	private void updatePeptideMetaData() {
		UserParamType userParam = null;
		
		if( getMinPeptideLength() > 0 ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:Minimum peptide length");
			userParam.setValue(""+getMinPeptideLength());
			data.addAnalysisParam(userParam);
		}
		if( getPeptideScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:Peptide score type");
			userParam.setValue(getPeptideScoreThreshold().getName());
			data.addAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("EhuBio:Peptide score threshold");
			userParam.setValue(getPeptideScoreThreshold().getValue()+"");
			data.addAnalysisParam(userParam);
		}
	}
	
	private void updateProteinMetaData() {
		UserParamType userParam = null;
		
		if( getProteinScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:Protein score type");
			userParam.setValue(getProteinScoreThreshold().getName());
			data.addAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("EhuBio:Protein score threshold");
			userParam.setValue(getProteinScoreThreshold().getValue()+"");
			data.addAnalysisParam(userParam);
		}
	}
	
	private void updateGroupMetaData() {
		UserParamType userParam = null;
		
		if( getGroupScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("EhuBio:Protein group score type");
			userParam.setValue(getGroupScoreThreshold().getName());
			data.addAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("EhuBio:Protein group score threshold");
			userParam.setValue(getGroupScoreThreshold().getValue()+"");
			data.addAnalysisParam(userParam);
		}
	}

	private static void unlinkGroup( ProteinGroup group ) {
		Set<Protein> proteins = new HashSet<>(group.getProteins());
		for( Protein protein : proteins )
			if( protein.getGroup() == group )
				unlinkProtein(protein);
	}

	private static void unlinkProtein(Protein protein) {
		Set<Peptide> peptides = new HashSet<>(protein.getPeptides());
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
		Set<Psm> psms = new HashSet<>(peptide.getPsms());
		peptide.getPsms().clear();
		for( Psm psm : psms )
			unlinkPsm(psm);		
		
		Set<Protein> proteins = new HashSet<>(peptide.getProteins());
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