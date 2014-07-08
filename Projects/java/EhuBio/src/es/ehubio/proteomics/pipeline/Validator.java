package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.proteomics.Decoyable;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
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
	
	private static final Logger logger = Logger.getLogger(Validator.class.getName());
	private final MsMsData data;	
	private boolean countDecoy = false;
	
	public Validator( MsMsData data ) {
		this.data = data;
	}

	public void updatePsmDecoyScores( ScoreType type ) {
		updateDecoyScores(data.getPsms(), type, ScoreType.PSM_P_VALUE, ScoreType.PSM_LOCAL_FDR, ScoreType.PSM_Q_VALUE, ScoreType.PSM_FDR_SCORE);
	}
	
	public void updatePeptideDecoyScores( ScoreType type ) {
		updateDecoyScores(data.getPeptides(), type, null, ScoreType.PEPTIDE_LOCAL_FDR, ScoreType.PEPTIDE_Q_VALUE, ScoreType.PEPTIDE_FDR_SCORE);
	}
	
	public void updateProteinDecoyScores( ScoreType type ) {
		updateDecoyScores(data.getProteins(), type, null, ScoreType.PROTEIN_LOCAL_FDR, ScoreType.PROTEIN_Q_VALUE, ScoreType.PROTEIN_FDR_SCORE);
	}
	
	public void updateGroupDecoyScores( ScoreType type ) {
		updateDecoyScores(data.getGroups(), type, null, ScoreType.GROUP_LOCAL_FDR, ScoreType.GROUP_Q_VALUE, ScoreType.GROUP_FDR_SCORE);
	}
	
	private void updateDecoyScores( Set<? extends Decoyable> set, ScoreType type, ScoreType pValue, ScoreType localFdr, ScoreType qValue, ScoreType fdrScore ) {
		List<Decoyable> list = new ArrayList<>();
		for( Decoyable item : set )
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
		for( int i = list.size()-1; i >= 0; i-- ) {
			item = list.get(i);
			if( Boolean.TRUE.equals(item.getDecoy()) )
				decoy++;
			else
				target++;
			ScoreGroup scoreGroup = new ScoreGroup();
			scoreGroup.setFdr(getFdr(decoy,target));
			if( pValue )
				scoreGroup.setpValue(((double)decoy)/totalDecoys);
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
		Set<Peptide> peptides = new HashSet<>();
		for( ProteinGroup group : data.getGroups() ) {
			if( group.getConfidence() == Protein.Confidence.NON_CONCLUSIVE )
				continue;
			peptides.clear();
			p = 1;			
			for( Protein protein : group.getProteins() )
				for( Peptide peptide : protein.getPeptides() )
					if( peptide.getConfidence() != Peptide.Confidence.NON_DISCRIMINATING )
						peptides.add(peptide);
			for( Peptide peptide : peptides )
				p *= peptide.getScoreByType(ScoreType.PEPTIDE_P_VALUE).getValue();
			group.setScore(new Score(ScoreType.GROUP_P_VALUE, p));
		}
	}
	
	public void updateProbabilities() {
		updatePeptideProbabilities();
		updateProteinProbabilities();
		updateGroupProbabilities();
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
	
	public void logFdrs() {
		logger.info(String.format("FDR -> PSM: %s, Peptide: %s, Protein: %s, Group: %s",
			getPsmFdr().getRatio(), getPeptideFdr().getRatio(), getProteinFdr().getRatio(), getGroupFdr().getRatio()));
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