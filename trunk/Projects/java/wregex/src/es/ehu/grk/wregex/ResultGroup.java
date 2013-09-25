package es.ehu.grk.wregex;

import java.util.Iterator;

public final class ResultGroup implements Iterable<Result> {
	ResultGroup( Iterable<Result> list ) {
		this.list = list;
	}
	
	@Override
	public Iterator<Result> iterator() {
		return list.iterator();
	}
	
	private final Iterable<Result> list;
}
