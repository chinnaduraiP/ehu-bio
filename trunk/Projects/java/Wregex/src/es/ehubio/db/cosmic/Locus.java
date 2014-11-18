package es.ehubio.db.cosmic;

import java.util.HashMap;
import java.util.Map;

import es.ehubio.model.Aminoacid;

public class Locus {
	private int position;
	private Aminoacid original;
	private Map<Aminoacid, Integer> counts = new HashMap<>();
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public Aminoacid getOriginal() {
		return original;
	}

	public void setOriginal(Aminoacid aa) {
		this.original = aa;
	}

	public int getMutations(Aminoacid aa) {
		Integer count = counts.get(aa);
		return count == null ? 0 : count.intValue();
	}

	public void incMutations(Aminoacid aa) {
		Integer count = counts.get(aa);
		if( count == null )
			count = 1;
		else
			count = count+1;
		counts.put(aa, count);
	}
	
	public int getMutations() {
		int count = 0;
		for( Integer mut : counts.values() )
			count += mut.intValue();
		return count;
	}
}
