package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.proteomics.Decoyable;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;

/**
 * Class for validating information about a proteomics experiment.
 * 
 * @author gorka
 *
 */
public final class Validator {
	public class FdrResult {
		private final int target;
		private final int decoy;
		private final double fdr;
		
		public FdrResult( int decoy, int target ) {
			this.decoy = decoy;
			this.target = target;			
			fdr = getFdr(decoy, target); 
		}

		public int getTarget() {
			return target;
		}

		public int getDecoy() {
			return decoy;
		}

		public double getRatio() {
			return fdr;
		}		
	}
	
	private static final Logger logger = Logger.getLogger(Validator.class.getName());
	private final MsMsData data;	
	private boolean countDecoy = false;
	
	public Validator( MsMsData data ) {
		this.data = data;
	}
	
	public void addDecoyScores( ScoreType type) {
		addPsmDecoyScores(type);
	}
	
	private void addPsmDecoyScores( ScoreType type) {
		List<Psm> list = new ArrayList<>(data.getPsms());
		sort(list,type);
		
		// Traverse from best to worst
		int decoy = 0;
		int target = 0;
		Map<Double,Double> mapFdr = new HashMap<>();
		for( int i = list.size()-1; i >= 0; i-- ) {
			Psm psm = list.get(i);
			if( Boolean.TRUE.equals(psm.getDecoy()) )
				decoy++;
			else
				target++;
			mapFdr.put(psm.getScoreByType(type).getValue(), getFdr(decoy,target));
		}
		
		// Traverse from worst to best
		Map<Double,Double> mapQValue = new HashMap<>();
		double min = mapFdr.get(list.get(0).getScoreByType(type).getValue());
		for( int i = 0; i < list.size(); i++ ) {
			Double score = list.get(i).getScoreByType(type).getValue();
			Double fdr = mapFdr.get(score);
			if( fdr < min )
				min = fdr;
			mapQValue.put(score, min);
		}
		
		// Interpolate q-values from best to worst
		int i = list.size()-1;
		double x0 = list.get(i).getScoreByType(type).getValue();
		double y0 = mapQValue.get(x0);
		Map<Double,Double> mapFValue = new HashMap<>();
		while( i > 0 ) {
			int j = i;
			double y1, x1;
			do {
				j--;
				x1 = list.get(j).getScoreByType(type).getValue();
				y1 = mapQValue.get(x1);  
			} while( j > 0 && y1 == y0 );
			double m = (y1-y0)/(x1-x0);
			for( int k = j; k <= i; k++ ) {
				double x = list.get(k).getScoreByType(type).getValue();
				mapFValue.put(x, (x-x0)*m+y0);
			}
			i=j;
		}
		
		// Assign scores
		for( Psm psm : data.getPsms() ) {
			psm.setScore(new Score(ScoreType.PSM_LOCAL_FDR,mapFdr.get(psm.getScoreByType(type).getValue())));
			psm.setScore(new Score(ScoreType.PSM_Q_VALUE,mapQValue.get(psm.getScoreByType(type).getValue())));
			psm.setScore(new Score(ScoreType.PSM_FDR_SCORE,mapFValue.get(psm.getScoreByType(type).getValue())));
		}
	}
	
	public boolean isCountDecoy() {
		return countDecoy;
	}

	/**
	 * If true uses FDR=2*D/(T+D), else FDR=D/T
	 * 
	 * @param countDecoy
	 */
	public void setCountDecoy(boolean countDecoy) {
		this.countDecoy = countDecoy;
	}
	
	public double getPsmFdrThreshold( ScoreType type, double threshold ) {
		return getFdrThreshold(data.getPsms(), type, threshold);
	}
	
	public double getPeptideFdrThreshold( ScoreType type, double threshold ) {
		return getFdrThreshold(data.getPeptides(), type, threshold);
	}
	
	public double getProteinFdrThreshold( ScoreType type, double threshold ) {
		return getFdrThreshold(data.getProteins(), type, threshold);
	}
	
	public double getGroupFdrThreshold( ScoreType type, double threshold ) {
		return getFdrThreshold(data.getGroups(), type, threshold);
	}	
	
	private double getFdrThreshold( Set<? extends Decoyable> set, ScoreType type, double threshold ) {
		if( set.isEmpty() )
			return 0.0;
		
		List<Decoyable> list = new ArrayList<>(set);		
		sort(list, type);
		
		//logger.info("Calculating score threshold ...");
		FdrResult orig = getSetFdr(set);
		int decoy = orig.getDecoy();
		int target = orig.getTarget();
		double fdr = orig.getRatio();
		int offset = 0;
		Decoyable item = list.get(offset);
		Score oldScore = item.getScoreByType(type);		
		while( fdr > threshold && offset < list.size()-1 ) {			
			Score nextScore;
			do {	// items with the same score
				if( Boolean.TRUE.equals(item.getDecoy()) )
					decoy--;
				else
					target--;
				offset++;
				item = list.get(offset);
				nextScore = item.getScoreByType(type);
			} while( offset < list.size()-1 && oldScore.compare(nextScore.getValue()) == 0 );
			oldScore = nextScore;
			fdr = getFdr(decoy, target);
			//System.out.println(String.format("Score=%s,  FDR=%s",score.getValue(),fdr));			
		}
		if( fdr > threshold )
			logger.warning("Desired FDR cannot be reached");
		//logger.info("done!");
		return oldScore.getValue();
	}
	
	private void sort( List<? extends Decoyable> list, ScoreType type ) {
		//logger.info("Sorting scores ...");
		Collections.sort(list, new Comparator<Decoyable>() {
			private ScoreType type;
			public Comparator<Decoyable> setType(ScoreType type) {
				this.type = type;
				return this;
			}
			@Override
			public int compare(Decoyable o1, Decoyable o2) {
				return o1.getScoreByType(type).compare(o2.getScoreByType(type).getValue());
			}
		}.setType(type));
	}
	
	public FdrResult getPsmFdr() {
		return getSetFdr(data.getPsms());
	}
	
	public FdrResult getPeptideFdr() {
		return getSetFdr(data.getPeptides());
	}
	
	public FdrResult getProteinFdr() {
		return getSetFdr(data.getProteins());
	}
	
	public FdrResult getGroupFdr() {
		return getSetFdr(data.getGroups());	
	}
	
	private FdrResult getSetFdr( Set<? extends Decoyable> set ) {
		int decoy = 0;
		int target = 0;
		for( Decoyable item : set ) {
			if( item.skipFdr() )
				continue;
			if( Boolean.TRUE.equals(item.getDecoy()) )
				decoy++;
			else
				target++;
		}
		return new FdrResult(decoy, target);
	}	
	
	private double getFdr( int decoy, int target ) {
		if( target == 0 )
			return 0.0;
		if( isCountDecoy() )
			return (2.0*decoy)/(target+decoy);
		return ((double)decoy)/target;
	}
}