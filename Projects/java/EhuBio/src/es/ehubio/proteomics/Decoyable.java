package es.ehubio.proteomics;

public interface Decoyable {
	/**
	 * 
	 * @return true if decoy, false if no decoy, and null if unknown
	 */
	Boolean getDecoy();
	
	/**
	 * 
	 * @param decoy true if decoy, false if no decoy, and null if unknown
	 */
	void setDecoy( Boolean decoy );
	
	/**
	 * 
	 * @param score
	 * @return true if the score has been added
	 */
	boolean addScore(Score score);
	
	/**
	 * Replaces all existing scores of the same type with the new score
	 * 
	 * @param score
	 */
	void setScore(Score score);
	
	/**
	 * 
	 * @param type
	 * @return The first score of the specified type
	 */
	Score getScoreByType( ScoreType type );
	
	/**
	 * 
	 * @return true to skip this object from FDR calculation
	 */
	boolean skipFdr();
}
