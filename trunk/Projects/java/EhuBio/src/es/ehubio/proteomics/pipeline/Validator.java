package es.ehubio.proteomics.pipeline;

import java.util.logging.Logger;

import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.pipeline.FdrCalculator.FdrResult;

/**
 * Class for validating information about a proteomics experiment.
 * 
 * @author gorka
 *
 */
public final class Validator {
	private static final Logger logger = Logger.getLogger(Validator.class.getName());
	private final MsMsData data;	
	private final FdrCalculator calc = new FdrCalculator();
	
	public Validator( MsMsData data ) {
		this.data = data;
	}

	public void updatePsmDecoyScores( ScoreType type ) {
		calc.updateDecoyScores(data.getPsms(), type, ScoreType.PSM_P_VALUE, ScoreType.PSM_LOCAL_FDR, ScoreType.PSM_Q_VALUE, ScoreType.PSM_FDR_SCORE);
	}
	
	public void updatePeptideDecoyScores( ScoreType type ) {
		calc.updateDecoyScores(data.getPeptides(), type, null, ScoreType.PEPTIDE_LOCAL_FDR, ScoreType.PEPTIDE_Q_VALUE, ScoreType.PEPTIDE_FDR_SCORE);
	}
	
	public void updateProteinDecoyScores( ScoreType type ) {
		calc.updateDecoyScores(data.getProteins(), type, null, ScoreType.PROTEIN_LOCAL_FDR, ScoreType.PROTEIN_Q_VALUE, ScoreType.PROTEIN_FDR_SCORE);
	}
	
	public void updateGroupDecoyScores( ScoreType type ) {
		calc.updateDecoyScores(data.getGroups(), type, null, ScoreType.GROUP_LOCAL_FDR, ScoreType.GROUP_Q_VALUE, ScoreType.GROUP_FDR_SCORE);
	}	
	
	public void updatePeptideProbabilities() {
		double p;		
		for( Peptide peptide : data.getPeptides() ) {
			p = 1;
			for( Psm psm : peptide.getPsms() )
				p *= psm.getScoreByType(ScoreType.PSM_P_VALUE).getValue();
			peptide.setScore(new Score(ScoreType.PEPTIDE_P_VALUE, p));
		}
	}
	
	public void updateProteinProbabilities() {
		double p;
		for( Protein protein : data.getProteins() ) {
			p = 1;
			for( Peptide peptide : protein.getPeptides() )
				p *= peptide.getScoreByType(ScoreType.PEPTIDE_P_VALUE).getValue();
			protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, p));
		}
	}
	
	public void updateGroupProbabilities() {
		double p;
		for( ProteinGroup group : data.getGroups() ) {
			if( group.getConfidence() == Protein.Confidence.NON_CONCLUSIVE )
				continue;
			p = 1;			
			for( Peptide peptide : group.getOwnPeptides() )
				p *= peptide.getScoreByType(ScoreType.PEPTIDE_P_VALUE).getValue();
			group.setScore(new Score(ScoreType.GROUP_P_VALUE, p));
			//group.setScore(new Score(ScoreType.GROUP_P_VALUE, group.getBestOwnPeptide(ScoreType.PEPTIDE_P_VALUE).getScoreByType(ScoreType.PEPTIDE_P_VALUE).getValue()));
		}
	}
	
	public void updateProbabilities() {
		updatePeptideProbabilities();
		updateProteinProbabilities();
		updateGroupProbabilities();
	}
	
	public boolean isCountDecoy() {
		return calc.isCountDecoy();
	}

	public void setCountDecoy(boolean countDecoy) {
		calc.setCountDecoy(countDecoy);
	}		
	
	public FdrResult getPsmFdr() {
		return calc.getFdr(data.getPsms());
	}
	
	public FdrResult getPeptideFdr() {
		return calc.getFdr(data.getPeptides());
	}
	
	public FdrResult getProteinFdr() {
		return calc.getFdr(data.getProteins());
	}
	
	public FdrResult getGroupFdr() {
		return calc.getFdr(data.getGroups());	
	}
	
	public void logFdrs() {
		logger.info(String.format("FDR -> PSM: %s, Peptide: %s, Protein: %s, Group: %s",
			getPsmFdr().getRatio(), getPeptideFdr().getRatio(), getProteinFdr().getRatio(), getGroupFdr().getRatio()));
	}	
}