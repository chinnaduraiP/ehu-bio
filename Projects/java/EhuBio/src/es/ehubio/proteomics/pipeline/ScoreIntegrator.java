package es.ehubio.proteomics.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import es.ehubio.proteomics.DecoyBase;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;

public class ScoreIntegrator {
	public static class IterativeResult {
		public IterativeResult(int iteration, Map<Protein, Map<Peptide,Double>> mapFactors) {
			this.iteration = iteration;
			this.mapFactors = mapFactors;
		}
		public final int iteration;
		public final Map<Protein, Map<Peptide,Double>> mapFactors;
	}
	
	public static void updatePsmScores( Collection<Psm> psms ) {
		for( Psm psm : psms ) {
			Score pValue = psm.getScoreByType(ScoreType.PSM_P_VALUE);
			Score spHpp = new Score(ScoreType.PSM_SPHPP_SCORE, -Math.log(pValue.getValue()));
			psm.addScore(spHpp);
		}
	}
	
	public static void psmToPeptide( Collection<Peptide> peptides ) {
		for( Peptide peptide : peptides )
			sumIntegrator(peptide, peptide.getPsms(), ScoreType.PSM_SPHPP_SCORE, ScoreType.PEPTIDE_SPHPP_SCORE);
	}
	
	public static void peptideToProteinEquitative( Collection<Protein> proteins ) {		
		for( Protein protein : proteins ) {
			double s = 0.0;
			for( Peptide peptide : protein.getPeptides() )
				s += peptide.getScoreByType(ScoreType.PEPTIDE_SPHPP_SCORE).getValue()/peptide.getProteins().size();
			protein.setScore(new Score(ScoreType.PROTEIN_SPHPP_SCORE, s));
			//protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, Math.exp(-s)));
		}
	}
	
	public static IterativeResult peptideToProteinIterative( Collection<Protein> proteins, double epsilon, int maxIters ) {
		Map<Protein, Map<Peptide,Double>> mapFactors = initFactors(proteins);
		int iteration = 0;
		while( iteration < maxIters && updateProteinScoresStep(proteins,mapFactors,epsilon) )
			logger.info(String.format("Iteration = %d",++iteration));
		return new IterativeResult(iteration, mapFactors);
	}
	
	public static void proteinToGroup( Collection<ProteinGroup> groups ) {
		for( ProteinGroup group : groups )
			sumIntegrator(group, group.getProteins(), ScoreType.PROTEIN_SPHPP_SCORE, ScoreType.GROUP_SPHPP_SCORE);
	}
	
	public static void divideRandom( Collection<Protein> proteins, RandomMatcher random ) {
		for( Protein protein : proteins ) {
			double Nq = random.getExpected(protein);
			Score score = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE);
			score.setValue(score.getValue()/Nq);
			//protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, Math.exp(-score.getValue())));
		}
	}
	
	public static void modelRandom( Collection<Protein> proteins, RandomMatcher random ) {
		logger.info("Modelling random peptide-protein matching ...");
		for( Protein protein : proteins ) {
			double Mq = random.getExpected(protein);
			if( Mq == 0 )
				throw new AssertionError(String.format("Mq=0 for %s", protein.getAccession()));
			PoissonDistribution poisson = new PoissonDistribution(Mq);
			//ExponentialDistribution exp = new ExponentialDistribution(Nq);
			Score score = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE);
			double LPQ = score.getValue();
			double sum = 1.0e-300;
			for( int n = 1; n <= 50; n++ ) {
				GammaDistribution gamma = new GammaDistribution(n, 1);
				sum += poisson.probability(n)*(1-gamma.cumulativeProbability(LPQ));
				//sum += exp.density(n)*(1-gamma.cumulativeProbability(pep));
			}			
			double LPQcorr = -Math.log(sum);
			/*if( Mq > 1.0 && (LPQcorr > LPQ || LPQcorr < LPQ/Mq) )
				//throw new AssertionError(String.format("Modelling error: %s <= %s <= %s not satisfied (Mq=%s)!!", LPQ/Mq, LPQcorr, LPQ, Mq));
				logger.warning(String.format("Modelling error: %s <= %s <= %s not satisfied (Mq=%s)!!", LPQ/Mq, LPQcorr, LPQ, Mq));*/
			score.setValue(LPQcorr);
			//protein.setScore(new Score(ScoreType.PROTEIN_P_VALUE, sum));
		}
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
	
	//private static void basicIntegrator(Collection<? extends DecoyBase> subitems, ScoreType lowS, DecoyBase item, ScoreType upP, ScoreType upS) {
	private static void sumIntegrator(DecoyBase item, Collection<? extends DecoyBase> subitems, ScoreType lowScore, ScoreType upScore) {
		double s = 0.0;
		for( DecoyBase subitem : subitems )
			s += subitem.getScoreByType(lowScore).getValue();
		//Score pValue = new Score(upP,Math.exp(-s));
		Score spHpp = new Score(upScore,s);
		//item.setScore(pValue);
		item.setScore(spHpp);
	}
	
	private static final Logger logger = Logger.getLogger(ScoreIntegrator.class.getName());
}