package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.ehubio.proteomics.DecoyBase;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;

public class ScoreIntegrator {
	//private static final Logger logger = Logger.getLogger(ScoreIntegrator.class.getName());
	
	public static void updatePsmScores( Collection<Psm> psms ) {
		for( Psm psm : psms ) {
			Score pValue = psm.getScoreByType(ScoreType.PSM_P_VALUE);
			Score spHpp = new Score(ScoreType.PSM_SPHPP_SCORE, -Math.log(pValue.getValue()));
			psm.addScore(spHpp);
		}
	}
	
	public static void updatePeptideScores( Collection<Peptide> peptides ) {
		for( Peptide peptide : peptides ) {
			basicIntegrator(
				peptide.getPsms(), ScoreType.PSM_P_VALUE, ScoreType.PSM_SPHPP_SCORE,
				peptide, ScoreType.PEPTIDE_P_VALUE, ScoreType.PEPTIDE_SPHPP_SCORE);
		}
	}
	
	public static void updateProteinScoresBasic( Collection<Protein> proteins ) {		
		for( Protein protein : proteins ) {
			double s = 0.0;
			for( Peptide peptide : protein.getPeptides() )
				s += peptide.getScoreByType(ScoreType.PEPTIDE_SPHPP_SCORE).getValue()/peptide.getProteins().size();
			protein.setScore(new Score(ScoreType.PROTEIN_SPHPP_SCORE, s));
			protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, Math.exp(-s)));
		}
	}
	
	/**
	 * Normalizes by the number of peptides in the corresponding decoy.
	 * 
	 * @param proteins
	 * @param decoyPrefix the decoy accession should be this prefix followed by the target accession.
	 */
	public static void updateProteinScoresAprox( Collection<Protein> proteins, String decoyPrefix ) {
		updateProteinScoresBasic(proteins);
		
		Map<String, Protein> mapDecoys = new HashMap<>();
		for( Protein protein : proteins )
			if( Boolean.TRUE.equals(protein.getDecoy()) )
				mapDecoys.put(protein.getAccession(), protein);
		for( Protein protein : proteins ) {
			int N = 1;
			if( Boolean.TRUE.equals(protein.getDecoy()) )
				N = protein.getPeptides().size();
			else {
				Protein decoy = mapDecoys.get(decoyPrefix+protein.getAccession());
				if( decoy != null )
					N = decoy.getPeptides().size();
			}
			if( N == 1 )
				continue;
			Score score = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE);
			score.setValue(score.getValue()/N);
			protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, Math.exp(-score.getValue())));
		}
	}	
	
	public static void updateGroupScoresBasic( Collection<ProteinGroup> groups ) {
		for( ProteinGroup group : groups ) {
			basicIntegrator(
				group.getProteins(), ScoreType.PROTEIN_P_VALUE, ScoreType.PROTEIN_SPHPP_SCORE,
				group, ScoreType.GROUP_P_VALUE, ScoreType.GROUP_SPHPP_SCORE);
		}
	}
	
	private static void basicIntegrator(Collection<? extends DecoyBase> subitems, ScoreType lowP, ScoreType lowS, DecoyBase item, ScoreType upP, ScoreType upS) {
		double p = 1.0;
		double s = 0.0;
		for( DecoyBase subitem : subitems ) {
			p *= subitem.getScoreByType(lowP).getValue();
			s += subitem.getScoreByType(lowS).getValue();
		}
		Score pValue = new Score(upP,p);
		Score spHpp = new Score(upS,s);
		item.setScore(pValue);
		item.setScore(spHpp);
	}	
}