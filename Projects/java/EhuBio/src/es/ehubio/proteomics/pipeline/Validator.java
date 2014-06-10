package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.proteomics.Decoyable;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Psm;

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
	private MsMsData data;	
	private boolean countDecoy = false;
	
	public void setData( MsMsData data ) {
		this.data = data;
	}
	
	public boolean isCountDecoy() {
		return countDecoy;
	}

	public void setCountDecoy(boolean countDecoy) {
		this.countDecoy = countDecoy;
	}
	
	public double getPsmFdrThreshold( Psm.ScoreType type, double threshold ) {
		return getFdrThreshold(data.getPsms(), type, threshold);
	}
	
	public double getPeptideFdrThreshold( Psm.ScoreType type, double threshold ) {
		return getFdrThreshold(data.getPeptides(), type, threshold);
	}
	
	public double getProteinFdrThreshold( Psm.ScoreType type, double threshold ) {
		return getFdrThreshold(data.getProteins(), type, threshold);
	}
	
	public double getGroupFdrThreshold( Psm.ScoreType type, double threshold ) {
		return getFdrThreshold(data.getGroups(), type, threshold);
	}
	
	private double getFdrThreshold( Set<? extends Decoyable> set, Psm.ScoreType type, double threshold ) {
		if( set.isEmpty() )
			return 0.0;
		
		List<Decoyable> list = new ArrayList<>(set);
		logger.info("Sorting PSMs ...");
		Collections.sort(list, new Comparator<Decoyable>() {
			private Psm.ScoreType type;
			public Comparator<Decoyable> setType(Psm.ScoreType type) {
				this.type = type;
				return this;
			}
			@Override
			public int compare(Decoyable o1, Decoyable o2) {
				return o1.getScoreByType(type).compare(o2.getScoreByType(type).getValue());
			}
		}.setType(type));
		
		logger.info("Calculating score threshold ...");
		FdrResult orig = getSetFdr(set);
		int decoy = orig.getDecoy();
		int target = orig.getTarget();
		double fdr = orig.getRatio();		
		Psm.Score score = list.get(0).getScoreByType(type);
		int offset = 0;
		while( fdr > threshold && offset < list.size() ) {
			Decoyable next;
			Psm.Score nextScore;
			do {
				next = list.get(offset);
				nextScore = next.getScoreByType(type);
				if( Boolean.TRUE.equals(next.getDecoy()) )
					decoy--;
				else
					target--;
				offset++;
			} while( offset < list.size() && score.compare(nextScore.getValue()) == 0 );
			score = nextScore;
			fdr = getFdr(decoy, target);
			//System.out.println(String.format("Score=%s,  FDR=%s",score.getValue(),fdr));			
		}
		logger.info("done!");
		return score.getValue();
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
			if( item.skip() )
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