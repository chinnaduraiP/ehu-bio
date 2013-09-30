package es.ehu.grk.wregex;

import java.util.Iterator;

public final class ResultGroup implements Iterable<Result> {
	ResultGroup( Iterable<Result> list ) {
		this.list = list;
		int size = 0;
		for(@SuppressWarnings("unused") Result result : list )
			size++;
		this.size = size;
	}
	
	@Override
	public Iterator<Result> iterator() {
		return list.iterator();
	}
	
	public int getSize() {
		return size;
	}

	private final Iterable<Result> list;
	private final int size;
}
