package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.ehu.grk.db.Aminoacid;
import es.ehu.grk.wregex.Pssm.PssmException;

public final class Trainer {
	Wregex wregex;
	List<TrainingGroup> trainingGroups = null;
	
	public Trainer( String regex ) {
		wregex = new Wregex(regex);
	}
	
	public List<TrainingGroup> train( List<InputMotif> motifs ) {
		List<ResultGroup> results;
		TrainingGroup group;
		trainingGroups = new ArrayList<>();
		for( InputMotif motif : motifs ) {
			results = wregex.searchGrouping(motif.fasta);
			for( ResultGroup result : results ) {
				group = new TrainingGroup(result, motif.getWeight());
				for( Result r : result ) {
					if( !motif.contains(r) )
						group.remove(r);
				}
				if( !group.isEmpty() )
					trainingGroups.add(group);
			}
		}
		
		return trainingGroups;
	}
	
	public Pssm buildPssm( boolean doNormalization ) throws PssmException {
		assert trainingGroups != null && !trainingGroups.isEmpty();
		Pssm pssm = new Pssm();
		List<TrainingMotif> motifs = new ArrayList<>();		
		for( TrainingGroup group : trainingGroups )
			motifs.addAll(group);
		int groupCount = motifs.get(0).getRegexGroups().size();		
		for( Aminoacid aa : Aminoacid.values() ) {
			Double[] scores = new Double[groupCount];
			for( int i = 0; i < groupCount; i++ ) {
				scores[i] = 0.0;
				double sum = 0.0;
				for( TrainingMotif motif : motifs ) {
					String str = motif.getRegexGroups().get(i).toUpperCase();
					sum += str.length()*motif.getDividedWeight();
					int last = 0;
					while( (last=str.indexOf(aa.letter, last)) != -1 ) {
						scores[i] += motif.getDividedWeight();
						last++;
					}
				}
				scores[i] = Math.log10(scores[i]/sum+0.00001); 
			}
			pssm.setScores(aa, Arrays.asList(scores));
		}
		if( doNormalization )
			pssm.normalize();
		return pssm;
	}
	
	public String getRegex() {
		return wregex.getRegex();
	}
}
