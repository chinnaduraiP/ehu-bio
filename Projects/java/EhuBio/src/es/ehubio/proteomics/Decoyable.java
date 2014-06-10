package es.ehubio.proteomics;

public interface Decoyable {
	Boolean getDecoy();
	Psm.Score getScoreByType( Psm.ScoreType type );
	boolean skip();
}
