package es.ehubio.db.cosmic;

import es.ehubio.model.Aminoacid;

public class Locus {	
	private int position;
	private Aminoacid original;
	private Aminoacid mutated;
	private int mutations;
	
	@Override
	public String toString() {
		return original.letter + "@" + position + ":" + mutations;
	}
	
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

	public int getMutations() {
		return mutations;
	}

	public void setMutations(int mutations) {
		this.mutations = mutations;
	}
	
	public void incMutations() {
		mutations++;
	}

	public Aminoacid getMutated() {
		return mutated;
	}

	public void setMutated(Aminoacid mutated) {
		this.mutated = mutated;
	}
}
