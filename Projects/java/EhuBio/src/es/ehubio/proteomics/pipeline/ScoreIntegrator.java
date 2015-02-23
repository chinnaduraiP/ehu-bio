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
import es.ehubio.proteomics.pipeline.RandomMatcher.Result;

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
			Score spHpp = new Score(ScoreType.LPS_SCORE, -Math.log10(pValue.getValue()));
			psm.addScore(spHpp);
		}
	}
	
	public static void psmToPeptide( Collection<Peptide> peptides ) {
		for( Peptide peptide : peptides )
			sumIntegrator(peptide, peptide.getPsms(), ScoreType.LPS_SCORE, ScoreType.LPP_SCORE);
	}
	
	public static void peptideToProteinEquitative( Collection<Protein> proteins ) {		
		for( Protein protein : proteins ) {
			double s = 0.0;
			double Mq = 0.0;
			for( Peptide peptide : protein.getPeptides() ) {
				double factor = 1.0/peptide.getProteins().size();
				Mq += factor;
				s += peptide.getScoreByType(ScoreType.LPP_SCORE).getValue()*factor;				
			}
			protein.setScore(new Score(ScoreType.LPQ_SCORE, s));
			protein.setScore(new Score(ScoreType.MQ_OVALUE, Mq));
			protein.setScore(new Score(ScoreType.NQ_OVALUE, protein.getPeptides().size()));
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
			sumIntegrator(group, group.getProteins(), ScoreType.LPQCORR_SCORE, ScoreType.LPG_SCORE);
	}
	
	private static void setExpectedValues( Collection<Protein> proteins, RandomMatcher random ) {
		/*List<Double> listNq = new ArrayList<>();
		List<Double> listMq = new ArrayList<>();
		for( Protein protein : proteins ) {			
			Result expected = random.getExpected(protein);
			if( expected.getNq() == 0 || expected.getMq() == 0 )
				throw new AssertionError(String.format("Mq=0 for %s, correct your search parameters", protein.getAccession()));
			listNq.add(protein.getScoreByType(ScoreType.NQ_OVALUE).getValue()/expected.getNq());
			listMq.add(protein.getScoreByType(ScoreType.MQ_OVALUE).getValue()/expected.getMq());
		}
		double Nr = Util.median(listNq);
		double Mr = Util.median(listMq);
		logger.info(String.format("Using correction factors: Nr=%s, Mr=%s",Nr,Mr));*/
		for( Protein protein : proteins ) {
			Result expected = random.getExpected(protein);
			if( expected.getNq() == 0 || expected.getMq() == 0 )
				throw new AssertionError(String.format("Mq=0 for %s, correct your search parameters", protein.getAccession()));
			/*protein.setScore(new Score(ScoreType.NQ_EVALUE, expected.getNq()*Nr));
			protein.setScore(new Score(ScoreType.MQ_EVALUE, expected.getMq()*Mr));*/
			protein.setScore(new Score(ScoreType.NQ_EVALUE, expected.getNq()));
			protein.setScore(new Score(ScoreType.MQ_EVALUE, expected.getMq()));
		}
	}
	
	public static void divideRandom( Collection<Protein> proteins, RandomMatcher random, boolean shared ) {
		setExpectedValues(proteins, random);
		for( Protein protein : proteins ) {
			Result expected = random.getExpected(protein);
			double Mq = shared ? protein.getScoreByType(ScoreType.MQ_EVALUE).getValue() : protein.getScoreByType(ScoreType.NQ_EVALUE).getValue();
			double LPQ = protein.getScoreByType(ScoreType.LPQ_SCORE).getValue();
			protein.setScore(new Score(ScoreType.LPQCORR_SCORE, LPQ/Mq));
			protein.setScore(new Score(ScoreType.MQ_EVALUE, expected.getMq()));
			protein.setScore(new Score(ScoreType.NQ_EVALUE, expected.getNq()));
		}
	}
	
	public static void modelRandom( Collection<Protein> proteins, RandomMatcher random, boolean shared ) {
		logger.info("Modelling random peptide-protein matching ...");
		setExpectedValues(proteins, random);
		double loge = Math.log(10.0);
		double epsilon = 1e-30;
		for( Protein protein : proteins ) {
			/*if( protein.getAccession().equals("decoy-genCDS_ENST00000296755_5_72107532-72205239_1"))
				System.out.println("Breakpoint");*/
			double Mq = shared ? protein.getScoreByType(ScoreType.MQ_EVALUE).getValue() : protein.getScoreByType(ScoreType.NQ_EVALUE).getValue();			
			double LPQ = protein.getScoreByType(ScoreType.LPQ_SCORE).getValue()*loge;
			
			int n1 = searchInf(Mq, LPQ, 1, (int)Math.round(Mq), 1, epsilon);
			int n2 = searchSup(Mq, LPQ, (int)Math.round(Mq), 10000, 1, epsilon);
			double sum = 1.0e-300;			
			PoissonDistribution poisson = new PoissonDistribution(Mq);
			for( int n = n1; n <= n2; n++ ) {
				GammaDistribution gamma = new GammaDistribution(n, 1);
				sum += poisson.probability(n)*(1-gamma.cumulativeProbability(LPQ));
			}
			
			double LPQcorr = -Math.log10(sum);
			protein.setScore(new Score(ScoreType.LPQCORR_SCORE, LPQcorr));
		}
	}
	
	private static int searchInf( double Mq, double LPQ, int n1, int n2, int dn, double epsilon ) {
		PoissonDistribution poisson = new PoissonDistribution(Mq);
		double p;
		int n=n1, prev=n1;		
		while( n2 - n > dn && n2 > n1) {			
			GammaDistribution gamma = new GammaDistribution(n, 1);
			p = poisson.probability(n)*(1-gamma.cumulativeProbability(LPQ));
			if( p < epsilon ) {
				prev = n;
				n = (n+n2)/2;
			} else {
				n2 = n;
				n = (prev+n2)/2;
			}
		};
		
		return n;
	}
	
	private static int searchSup( double Mq, double LPQ, int n1, int n2, int dn, double epsilon ) {
		PoissonDistribution poisson = new PoissonDistribution(Mq);
		double p;
		int n=n2, prev=n2;		
		while( n-n1 > dn && n1 < n2) {			
			GammaDistribution gamma = new GammaDistribution(n, 1);
			p = poisson.probability(n)*(1-gamma.cumulativeProbability(LPQ));
			if( p < epsilon ) {
				prev = n;
				n = (n+n1)/2;
			} else {
				n1 = n;
				n = (prev+n1)/2;
			}
		};
		
		return n;
	}
	
	private static Map<Protein, Map<Peptide,Double>> initFactors( Collection<Protein> proteins ) {
		Map<Protein, Map<Peptide,Double>> mapFactors = new HashMap<>();
		for( Protein protein : proteins ) {
			double score = 0.0;
			double Mq = 0.0;
			Map<Peptide,Double> scores = new HashMap<>();
			for( Peptide peptide : protein.getPeptides() ) {
				double factor = 1.0/peptide.getProteins().size();
				Mq += factor;
				scores.put(peptide, factor);
				score += factor*peptide.getScoreByType(ScoreType.LPP_SCORE).getValue();
			}
			protein.setScore(new Score(ScoreType.LPQ_SCORE, score));
			protein.setScore(new Score(ScoreType.MQ_OVALUE, Mq));
			protein.setScore(new Score(ScoreType.NQ_OVALUE, protein.getPeptides().size()));
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
				newScore += mapFactors.get(protein).get(peptide)*peptide.getScoreByType(ScoreType.LPP_SCORE).getValue();
			Score score = protein.getScoreByType(ScoreType.LPQ_SCORE);			
			if( !changed && Math.abs(newScore-score.getValue()) > epsilon )
				changed = true;
			score.setValue(newScore);
		}
		
		return changed;
	}

	private static void updateFactors( Collection<Protein> proteins, Map<Protein, Map<Peptide,Double>> mapFactors ) {
		for( Protein protein : proteins ) {
			double num = protein.getScoreByType(ScoreType.LPQ_SCORE).getValue();
			double Mq = 0.0;
			for( Peptide peptide : protein.getPeptides() ) {
				double den = 0.0;
				for( Protein protein2 : peptide.getProteins() )
					den += protein2.getScoreByType(ScoreType.LPQ_SCORE).getValue();
				double factor = num/den;
				Mq += factor;
				mapFactors.get(protein).put(peptide, factor);
			}
			protein.getScoreByType(ScoreType.MQ_OVALUE).setValue(Mq);
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