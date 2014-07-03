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
	private boolean passThreshold = false;
	private int minPeptideLength = 0;
	private Boolean filterDecoyPeptides;
	private int rankTreshold = 0;
	private Double ppmThreshold;
	private final MsMsData data;
	private final static int MAXITER=15;
	
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
		return filterDecoyPeptides == null ? false : filterDecoyPeptides;
	}

	public void setFilterDecoyPeptides(boolean filterDecoyPeptides) {
		this.filterDecoyPeptides = filterDecoyPeptides;
	}

	public boolean isPassThreshold() {
		return passThreshold;
	}

	public void setPassThreshold(boolean mzidPassThreshold) {
		this.passThreshold = mzidPassThreshold;
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
	
	public double runPsmFdrThreshold( ScoreType type, double fdr) {
		// Initial filter
		setFilterDecoyPeptides(false);
		run();
		logCounts("Filter");
		
		// FDR filter
		Validator validator = new Validator(data);
		double th = validator.getPsmFdrThreshold(type, fdr);
		Score score = new Score(type, th);
		setPsmScoreThreshold(score);
		run();
		logCounts(String.format("PSM FDR=%s (%s th=%s)", validator.getPsmFdr().getRatio(), type.getName(), th));
		
		// Decoy removal
		setFilterDecoyPeptides(true);
		run();
		logCounts("Decoy removal");
		
		CVParamType cv = new CVParamType();
		cv.setAccession("MS:1002260");
		cv.setCvRef("PSI-MS");
		cv.setName("PSM:FDR threshold");
		cv.setValue(String.format("%s",fdr));
		data.setThreshold(cv);
		
		return th;
	}	
	
	public double runGroupFdrThreshold( ScoreType type, double fdr) {
		// Initial filter and update groups using PAnalyzer
		PAnalyzer pAnalyzer = new PAnalyzer(data);				
		pAnalyzer.run();
		setFilterDecoyPeptides(false);
		run();
		pAnalyzer.run();
		
		// FDR Initialization
		Validator validator = new Validator(data);
		double prevThreshold;
		double newThreshold = validator.getGroupFdrThreshold(type, fdr);		
		Score score = new Score(type, newThreshold);
		setGroupScoreThreshold(score);
		
		// Iteration
		int i = 0;
		do {
			prevThreshold = newThreshold;
			run();
			pAnalyzer.run();
			newThreshold = validator.getGroupFdrThreshold(type, fdr);
			score.setValue(newThreshold);
			logger.info(String.format("Iteration: %s -> prev=%s, new=%s", ++i, prevThreshold, newThreshold));
		} while( type.compare(newThreshold, prevThreshold) > 0 && i < MAXITER );
		
		// Decoy removal
		setFilterDecoyPeptides(true);
		run();
		pAnalyzer.run();
		
		if( type.compare(newThreshold, prevThreshold) > 0 )
			logger.warning("Maximum number of iterations reached!");
		
		return prevThreshold;
	}
	
	private void logCounts( String title ) {
		logger.info(String.format("%s: %s", title, data.toString()));
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
			if( isPassThreshold() && !psm.isPassThreshold() ) {
				unlinkPsm(psm);
				continue;
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
			if( isPassThreshold() && !peptide.isPassThreshold() ) {
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
		for( Protein protein : data.getProteins() ) {
			if( isPassThreshold() && !protein.isPassThreshold() ) {
				unlinkProtein(protein);
				continue;
			}
			if( getProteinScoreThreshold() == null )
				continue;
			Score score = protein.getScoreByType(getProteinScoreThreshold().getType());
			if( score == null || getProteinScoreThreshold().compare(score.getValue()) > 0 )
				unlinkProtein(protein);
		}
	}

	private void filterGroups() {		
		for( ProteinGroup group : data.getGroups() ) {
			if( isPassThreshold() && !group.isPassThreshold() ) {
				unlinkGroup(group);
				continue;
			}
			if( getGroupScoreThreshold() == null )
				continue;
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
		cvParam.setValue(""+(filterDecoyPeptides!=null));
		data.setAnalysisParam(cvParam);
	}

	private void updatePsmMetaData() {
		UserParamType userParam = null;
		
		if( getPsmScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:PSM score type");
			userParam.setValue(getPsmScoreThreshold().getName());
			data.setAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:PSM score threshold");
			userParam.setValue(getPsmScoreThreshold().getValue()+"");
			data.setAnalysisParam(userParam);
		}
		if( getRankTreshold() > 0 ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:PSM rank threshold");
			userParam.setValue(getRankTreshold()+"");
			data.setAnalysisParam(userParam);
		}
		if( getPpmThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:PSM ppm threshold");
			userParam.setValue(getPpmThreshold()+"");
			data.setAnalysisParam(userParam);
		}
		
		userParam = new UserParamType();
		userParam.setName("PAnalyzer:Using search engine PSM threshold");
		userParam.setValue(isPassThreshold()+"");
		data.setAnalysisParam(userParam);
	}
	
	private void updatePeptideMetaData() {
		UserParamType userParam = null;
		
		if( getMinPeptideLength() > 0 ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Minimum peptide length");
			userParam.setValue(""+getMinPeptideLength());
			data.setAnalysisParam(userParam);
		}
		if( getPeptideScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Peptide score type");
			userParam.setValue(getPeptideScoreThreshold().getName());
			data.setAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Peptide score threshold");
			userParam.setValue(getPeptideScoreThreshold().getValue()+"");
			data.setAnalysisParam(userParam);
		}
		if( filterDecoyPeptides != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Decoys removed");
			userParam.setValue(isFilterDecoyPeptides()+"");
			data.setAnalysisParam(userParam);
		}
	}
	
	private void updateProteinMetaData() {
		UserParamType userParam = null;
		
		if( getProteinScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Protein score type");
			userParam.setValue(getProteinScoreThreshold().getName());
			data.setAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Protein score threshold");
			userParam.setValue(getProteinScoreThreshold().getValue()+"");
			data.setAnalysisParam(userParam);
		}
	}
	
	private void updateGroupMetaData() {
		UserParamType userParam = null;
		
		if( getGroupScoreThreshold() != null ) {
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Protein group score type");
			userParam.setValue(getGroupScoreThreshold().getName());
			data.setAnalysisParam(userParam);
			userParam = new UserParamType();
			userParam.setName("PAnalyzer:Protein group score threshold");
			userParam.setValue(getGroupScoreThreshold().getValue()+"");
			data.setAnalysisParam(userParam);
		}
	}

	private static void unlinkGroup( ProteinGroup group ) {
		group.setPassThreshold(false);
		Set<Protein> proteins = new HashSet<>(group.getProteins());
		for( Protein protein : proteins )
			if( protein.getGroup() == group )
				unlinkProtein(protein);
	}

	private static void unlinkProtein(Protein protein) {
		protein.setPassThreshold(false);
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
		peptide.setPassThreshold(false);
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
		psm.setPassThreshold(false);
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