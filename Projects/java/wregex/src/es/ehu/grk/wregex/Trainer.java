package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.List;

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
}
