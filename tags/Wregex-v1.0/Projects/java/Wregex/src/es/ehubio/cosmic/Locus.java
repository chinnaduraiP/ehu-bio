package es.ehubio.cosmic;

import es.ehubio.db.Aminoacid;

public class Locus {
	public int position;
	public Aminoacid aa;
	public int mutations;
	
	@Override
	public String toString() {
		return aa.letter + "@" + position + ":" + mutations;
	}
}
