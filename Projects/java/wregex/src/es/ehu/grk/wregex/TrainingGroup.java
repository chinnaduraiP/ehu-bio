package es.ehu.grk.wregex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TrainingGroup implements Collection<TrainingMotif> {
	public TrainingGroup( ResultGroup group, double weight ) {
		list = new ArrayList<TrainingMotif>();
		for( Result r : group )
			list.add(new TrainingMotif(r.fasta, r.index, r.end, weight));
		updateCombinations();
	}
	
	private void updateCombinations() {
		for( TrainingMotif m : list )
			m.setCombinations(list.size());
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<TrainingMotif> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(TrainingMotif e) {
		if( !list.add(e) )
			return false;
		updateCombinations();
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if( !list.remove(o) )
			return false;
		updateCombinations();
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends TrainingMotif> c) {
		if( !list.addAll(c) )
			return false;
		updateCombinations();
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if( !list.removeAll(c) )
			return false;
		updateCombinations();
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if( !list.retainAll(c) )
			return false;
		updateCombinations();
		return true;
	}

	@Override
	public void clear() {
		list.clear();
	}

	private List<TrainingMotif> list;
}
