package es.ehubio.db.cosmic;

import es.ehubio.model.Aminoacid;

public class Locus {
	public int position;
	public Aminoacid aa;
	public int mutations;
	
	@Override
	public String toString() {
		return aa.letter + "@" + position + ":" + mutations;
	}
}
