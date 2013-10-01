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
	
	public Result getRespresentative() {
		Result result = null;
		for( Result tmp : this ) {
			if( result == null ) {
				result = tmp;
				continue;
			}
			if( tmp.getScore() > result.getScore() ) {
				result = tmp;
				continue;
			}
			if( tmp.getScore() == result.getScore() && tmp.getMatch().length() > result.getMatch().length() )
				result = tmp;
		}
		return result;
	}

	private final Iterable<Result> list;
	private final int size;
}
