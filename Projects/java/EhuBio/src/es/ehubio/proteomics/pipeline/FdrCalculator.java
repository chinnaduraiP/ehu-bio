package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ehubio.proteomics.Decoyable;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;

public class FdrCalculator {
	private boolean countDecoy = false;

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
	
	public double getFdr( int decoy, int target ) {
		if( target == 0 )
			return 0.0;
		if( isCountDecoy() )
			return (2.0*decoy)/(target+decoy);
		return ((double)decoy)/target;
	}
	
	public void updateDecoyScores( Collection<? extends Decoyable> items, ScoreType type, ScoreType pValue, ScoreType localFdr, ScoreType qValue, ScoreType fdrScore ) {
		List<Decoyable> list = new ArrayList<>();
		for( Decoyable item : items )
			if( !item.skipFdr() )
				list.add(item);
		sort(list,type);
		Map<Double,ScoreGroup> mapScores = new HashMap<>();

		getLocalFdr(list,type,pValue!=null,mapScores);
		getQValues(list,type,mapScores);
		if( fdrScore != null )
			getFdrScores(list,type,mapScores);		
		
		// Assign scores
		for( Decoyable item : list ) {
			ScoreGroup scoreGroup = mapScores.get(item.getScoreByType(type).getValue());
			if( pValue != null )
				item.setScore(new Score(pValue, scoreGroup.getpValue()));
			item.setScore(new Score(localFdr,scoreGroup.getFdr()));
			item.setScore(new Score(qValue,scoreGroup.getqValue()));
			if( fdrScore != null )
				item.setScore(new Score(fdrScore,scoreGroup.getFdrScore()));
			//System.out.println(String.format("%s,%s,%s,%s,%s",psm.getScoreByType(type).getValue(),scoreGroup.getpValue(),scoreGroup.getFdr(),scoreGroup.getqValue(),scoreGroup.getFdrScore()));
		}
	}
	
	public FdrResult getFdr( Collection<? extends Decoyable> items ) {
		int decoy = 0;
		int target = 0;
		for( Decoyable item : items ) {
			if( item.skipFdr() )
				continue;
			if( Boolean.TRUE.equals(item.getDecoy()) )
				decoy++;
			else
				target++;
		}
		return new FdrResult(decoy, target);
	}
	
	private void sort( List<? extends Decoyable> list, final ScoreType type ) {
		//logger.info("Sorting scores ...");
		Collections.sort(list, new Comparator<Decoyable>() {
			@Override
			public int compare(Decoyable o1, Decoyable o2) {
				return o1.getScoreByType(type).compare(o2.getScoreByType(type).getValue());
			}
		});
	}
	
	private void getLocalFdr(List<Decoyable> list, ScoreType type, boolean pValue, Map<Double,ScoreGroup> mapScores) {
		// Count total decoy number (for p-values)
		int totalDecoys = 0;
		if( pValue )
			for( Decoyable item : list )
				if( Boolean.TRUE.equals(item.getDecoy()) )
					totalDecoys++;

		// Traverse from best to worst to calculate local FDRs and p-values
		int decoy = 0;
		int target = 0;
		Decoyable item;
		double pOff;
		for( int i = list.size()-1; i >= 0; i-- ) {
			item = list.get(i);
			if( Boolean.TRUE.equals(item.getDecoy()) ) {
				decoy++;
				pOff = -0.5;
			} else {
				target++;
				pOff = 0.5;
			}
			ScoreGroup scoreGroup = new ScoreGroup();
			scoreGroup.setFdr(getFdr(decoy,target));
			if( pValue )
				scoreGroup.setpValue(totalDecoys==0?0:(decoy+pOff)/totalDecoys);
			mapScores.put(item.getScoreByType(type).getValue(), scoreGroup);
		}
	}
	
	private void getQValues(List<Decoyable> list, ScoreType type, Map<Double, ScoreGroup> mapScores) {
		// Traverse from worst to best to calculate q-values
		double min = mapScores.get(list.get(0).getScoreByType(type).getValue()).getFdr();		
		for( int i = 0; i < list.size(); i++ ) {
			ScoreGroup scoreGroup = mapScores.get(list.get(i).getScoreByType(type).getValue());
			double fdr = scoreGroup.getFdr();
			if( fdr < min )
				min = fdr;
			scoreGroup.setqValue(min);
		}
	}
	
	private void getFdrScores(List<Decoyable> list, ScoreType type, Map<Double, ScoreGroup> mapScores) {
		// Interpolate q-values from best to worst to calculate FDRScores
		int j, i = list.size()-1;
		double x1 = list.get(i).getScoreByType(type).getValue();
		double y1 = mapScores.get(x1).getqValue();
		double x0, y0, x, m;
		while( i > 0 ) {
			x0 = x1;
			y0 = y1;
			j = i;
			do {
				j--;
				x1 = list.get(j).getScoreByType(type).getValue();
				y1 = mapScores.get(x1).getqValue();  
			} while( j > 0 && y1 == y0 );
			m = (y1-y0)/(x1-x0);
			for( int k = j; k <= i; k++ ) {
				x = list.get(k).getScoreByType(type).getValue();
				mapScores.get(x).setFdrScore((x-x0)*m+y0);
			}
			i=j;
		}
	}
	
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
	
	private class ScoreGroup {
		private double pValue;
		private double fdr;
		private double qValue;
		private double fdrScore;
		public double getFdr() {
			return fdr;
		}
		public void setFdr(double fdr) {
			this.fdr = fdr;
		}
		public double getqValue() {
			return qValue;
		}
		public void setqValue(double qValue) {
			this.qValue = qValue;
		}
		public double getFdrScore() {
			return fdrScore;
		}
		public void setFdrScore(double fdrScore) {
			this.fdrScore = fdrScore;
		}
		public double getpValue() {
			return pValue;
		}
		public void setpValue(double pValue) {
			this.pValue = pValue;
		}
	}
}
