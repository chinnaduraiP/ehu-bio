package es.ehubio.proteomics;

import java.util.HashSet;
import java.util.Set;

public abstract class DecoyBase implements Decoyable {
	private Boolean decoy = null;
	private Set<Score> scores = new HashSet<>();
	private boolean passThreshold = true;

	@Override
	public boolean addScore(Score score) {
		return scores.add(score);
	}

	@Override
	public void setScore(Score score) {
		Score remove;
		do {
			remove = getScoreByType(score.getType());
			if( remove != null )
				scores.remove(remove);
		} while( remove != null );
		addScore(score);
	}

	@Override
	public Score getScoreByType(ScoreType type) {
		for( Score score : scores )
			if( score.getType() == type )
				return score;
		return null;
	}
	
	public Set<Score> getScores() {
		return scores;
	}

	@Override
	public boolean skipFdr() {
		return false;
	}

	@Override
	public Boolean getDecoy() {
		return decoy;
	}

	@Override
	public void setDecoy(Boolean decoy) {
		this.decoy = decoy;
	}
	
	@Override
	public void setPassThreshold(boolean passThreshold) {
		this.passThreshold = passThreshold;
	}
	
	@Override
	public boolean isPassThreshold() {
		return passThreshold;
	}
	
	public abstract String getUniqueString();
}