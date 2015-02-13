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
			Score spHpp = new Score(ScoreType.PSM_SPHPP_SCORE, -Math.log10(pValue.getValue()));
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
		}
	}
	
	public static void modelRandom( Collection<Protein> proteins, RandomMatcher random ) {
		logger.info("Modelling random peptide-protein matching ...");
		double loge = Math.log(10.0);
		//try{
		//PrintWriter pw = new PrintWriter(String.format("/home/gorka/model%s.csv", proteins.hashCode()));
		for( Protein protein : proteins ) {
			double Mq = random.getExpected(protein);
			if( Mq == 0 )
				throw new AssertionError(String.format("Mq=0 for %s", protein.getAccession()));			
			Score score = protein.getScoreByType(ScoreType.PROTEIN_SPHPP_SCORE);
			double LPQ = score.getValue();
			
			int[] range = getRange(Mq, LPQ);
			double sum = 1.0e-300;			
			PoissonDistribution poisson = new PoissonDistribution(Mq);
			for( int n = range[0]; n <= range[1]; n++ ) {
				GammaDistribution gamma = new GammaDistribution(n, 1);
				sum += poisson.probability(n)*(1-gamma.cumulativeProbability(LPQ*loge));
			}
			
			double LPQcorr = -Math.log10(sum);
			score.setValue(LPQcorr);
			
			//pw.println(String.format("%s,%s,%s,%s,%s,%s",protein.getAccession(),Mq,LPQ,LPQcorr,range[0],range[1]));
		}
		//pw.close();
		//} catch( Exception e ) {};
	}
	
	private static int[] getRange( double Mq, double LPQ ) {
		//return new int[]{1,50};
		//return new int[]{1,1000};
		
		int p1, p2;
		if( Mq < 7 ) {
			p1 = 1;
			p2 = 20;
		} else {
			double rangePoisson = 5.3*Math.pow(Mq,-0.55)*Mq; // Poisson > 0.00001
			p1 = (int)Math.round(Mq-rangePoisson);
			p2 = (int)Math.round(Mq+rangePoisson);		
			if( p1 < 1 ) p1 = 1;
		}
		
		int g1, g2;
		if( LPQ < 8 ) {
			g1 = 1;
			g2 = 20;
		} else {
			double rangeGamma = 4.4*Math.pow(LPQ,-0.50)*LPQ; // Gamma > 0.00001
			g1 = (int)Math.round(LPQ-rangeGamma);
			g2 = (int)Math.round(LPQ+rangeGamma);		
			if( g1 < 1 ) g1 = 1;
		}
		
		if( p2 < g1 || p1 > g2 )
			return new int[]{1,0};
		int[] range = new int[2];
		range[0] = p1 < g1 ? p1 : g1;
		range[1] = p2 > g2 ? p2 : g2;
		return range;
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
	
	private static void sumIntegrator(DecoyBase item, Collection<? extends DecoyBase> subitems, ScoreType lowScore, ScoreType upScore) {
		double s = 0.0;
		for( DecoyBase subitem : subitems )
			s += subitem.getScoreByType(lowScore).getValue();
		Score spHpp = new Score(upScore,s);
		item.setScore(spHpp);
	}
	
	private static final Logger logger = Logger.getLogger(ScoreIntegrator.class.getName());
}