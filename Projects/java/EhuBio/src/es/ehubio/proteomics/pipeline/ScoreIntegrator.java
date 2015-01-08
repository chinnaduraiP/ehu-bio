package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
				peptide.getPsms(), ScoreType.PSM_SPHPP_SCORE,
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
	public static void updateProteinScoresPrefix( Collection<Protein> proteins, String decoyPrefix ) {
		updateProteinScoresBasic(proteins);
		normalizeProteinScores(proteins, decoyPrefix);
	}
	
	private static void normalizeProteinScores( Collection<Protein> proteins, String decoyPrefix ) {
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
	
	public static int updateProteinScoresIter( Collection<Protein> proteins, String decoyPrefix, double epsilon, int maxIters ) {
		Map<Protein, Map<Peptide,Double>> mapFactors = initFactors(proteins);
		int iteration = 0;
		while( iteration < maxIters && updateProteinScoresStep(proteins,mapFactors,epsilon) )
			logger.info(String.format("Iteration = %d",++iteration));
		normalizeProteinScores(proteins, decoyPrefix);
		return iteration;
	}
	
	private static Map<Protein, Map<Peptide,Double>> initFactors( Collection<Protein> proteins ) {
		Map<Protein, Map<Peptide,Double>> mapFactors = new HashMap<>();
		for( Protein protein : proteins ) {
			double score = 0.0;
			Map<Peptide,Double> scores = new HashMap<>();
			for( Peptide peptide : protein.getPeptides() ) {
				double factor = 1.0/peptide.getProteins().size();
				scores.put(peptide, factor);
				score += factor*peptide.getScoreByType(ScoreType.PEPTIDE_SPHPP_SCORE).getValue();
			}
			protein.setScore(new Score(ScoreType.PROTEIN_SPHPP_SCORE, score));
			mapFactors.put(protein, scores);
		}
		return mapFactors;
	}
	
	private static boolean updateProteinScoresStep( Collection<Protein> proteins, Map<Protein, Map<Peptide,Double>> mapFactors, double epsilon ) {
		updateFactors(proteins, mapFactors);
		
		boolean changed = false;		
		for( Protein protein : proteins ) {
			double newScore = 0.0;
			for( Peptide peptide : protein.getPeptides() )
				newScore += mapFactors.get(protein).get(peptide)*peptide.getScoreByType(ScoreType.PEPTIDE_SPHPP_SCORE).getValue();
			Score score = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE);			
			if( !changed && Math.abs(newScore-score.getValue()) > epsilon )
				changed = true;
			score.setValue(newScore);
		}
		
		return changed;
	}

	private static void updateFactors( Collection<Protein> proteins, Map<Protein, Map<Peptide,Double>> mapFactors ) {
		for( Protein protein : proteins ) {
			double num = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE).getValue();
			for( Peptide peptide : protein.getPeptides() ) {
				double den = 0.0;
				for( Protein protein2 : peptide.getProteins() )
					den += protein2.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE).getValue();				
				mapFactors.get(protein).put(peptide, num/den);
			}
		}
	}
	
	public static void updateGroupScoresBasic( Collection<ProteinGroup> groups ) {
		for( ProteinGroup group : groups ) {
			basicIntegrator(
				group.getProteins(), ScoreType.PROTEIN_SPHPP_SCORE,
				group, ScoreType.GROUP_P_VALUE, ScoreType.GROUP_SPHPP_SCORE);
		}
	}
	
	private static void basicIntegrator(Collection<? extends DecoyBase> subitems, ScoreType lowS, DecoyBase item, ScoreType upP, ScoreType upS) {
		double s = 0.0;
		for( DecoyBase subitem : subitems )
			s += subitem.getScoreByType(lowS).getValue();
		Score pValue = new Score(upP,Math.exp(-s));
		Score spHpp = new Score(upS,s);
		item.setScore(pValue);
		item.setScore(spHpp);
	}
	
	private static final Logger logger = Logger.getLogger(ScoreIntegrator.class.getName());
}