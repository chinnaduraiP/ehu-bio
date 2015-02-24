package es.ehubio;

import java.util.Collections;
import java.util.List;

public class MathUtil {
	public static double median( List<Double> list ) {
		Collections.sort(list);
		int i = list.size()/2;
		if( list.size()%2 == 1 )
			return list.get(i);
		return (list.get(i)+list.get(i+1))/2;
	}
	
	public static double pow2(double num) {
		return num*num;
	}
}
