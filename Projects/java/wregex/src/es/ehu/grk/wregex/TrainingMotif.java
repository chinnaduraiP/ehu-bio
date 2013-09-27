package es.ehu.grk.wregex;

import java.util.List;

public final class TrainingMotif {
	private final Result result;
	private final TrainingGroup group;
	
	public TrainingMotif( Result result, TrainingGroup group ) {
		this.result = result;
		this.group = group;
	}
	
	public Result getResult() {
		return result;
	}
	
	public String getName() {
		return result.name;
	}
	
	public int getStart() {
		return result.start;
	}
	
	public int getEnd() {
		return result.end;
	}
	
	public String getMotif() {
		return result.match;
	}
	
	public String getAlignment() {
		return result.alignment;
	}
	
	public double getWeight() {
		return group.getWeight();
	}
	
	public int getCombinations() {
		return group.size();
	}
	
	public double getDividedWeight() {
		if( !isValid() )
			return 0.0;
		return getWeight()/getCombinations();
	}
	
	public boolean overlaps(Result result) {
		return this.result.overlaps(result);
	}
	
	public boolean linked(Result result) {
		return this.result.equals(result);
	}

	public TrainingGroup getGroup() {
		return group;
	}
	
	public List<String> getRegexGroups() {
		return result.getGroups();
	}
	
	public void remove() {
		group.remove(this);
	}
	
	public void recycle() {
		if( !group.contains(this) )
			group.add(this);
	}
	
	public boolean isValid() {
		return group.contains(this);
	}
}
