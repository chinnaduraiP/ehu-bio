package es.ehu.grk.wregex;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ehu.grk.db.Aminoacid;
import es.ehu.grk.io.UnixCfgReader;

public class Pssm {
	public class PssmException extends Exception {
		private static final long serialVersionUID = 1L;

		public PssmException(String msg) {
			super(msg);
		}
	}
	
	public Pssm() {		
	}
	
	public Pssm( Reader reader, boolean doNormalization ) throws IOException, PssmException {
		load(reader, doNormalization);
	}
	
	public void load(Reader reader, boolean doNormalization) throws IOException, PssmException {
		UnixCfgReader rd = new UnixCfgReader(reader);
		String str;
		String[] fields;
		int i;
		List<Double> scores = new ArrayList<>();
		while( (str=rd.readLine()) != null ) {
			fields = str.split("[ \t]");
			scores.clear();
			for( i = 1; i < fields.length; i++ )
				scores.add(Double.parseDouble(fields[i]));
			setScores(Aminoacid.parseLetter(fields[0].charAt(0)), scores);
		}
		if( doNormalization )
			normalize();
	}
	
	public void normalize() {
		double max;
		for( int i = 0; i < groups; i++ ) {
			max = -1000;
			for( Aminoacid aa : pssm.keySet() )
				if( pssm.get(aa).get(i) > max )
					max = pssm.get(aa).get(i);
			for( Aminoacid aa : pssm.keySet() )
				pssm.get(aa).set(i, pssm.get(aa).get(i)-max);
		}				
	}
	
	public void save(Writer writer, String... header) {
		DecimalFormat df=new DecimalFormat("0");
		PrintWriter pw = new PrintWriter(writer);
		for( String str : header )
			pw.println("# " + str);
		for( Aminoacid aa : pssm.keySet() ) {
			pw.print(aa.letter);
			for( Double score : pssm.get(aa) )
				pw.print("\t" + df.format(score));
			pw.println();
		}
		pw.flush();
	}
	
	public void setScores( Aminoacid aa, Collection<Double> scores ) throws PssmException {
		if( pssm.isEmpty() )
			groups = scores.size();
		else if( scores.size() != groups )
			throw new PssmException("Group count does not match");
		List<Double> list = pssm.get(aa);
		if( list == null ) {
			list = new ArrayList<>(scores);
			pssm.put(aa, list);
		} else {
			list.clear();
			list.addAll(scores);
		}
	}
	
	public double getScore(Aminoacid aa, int pos) throws PssmException {
		List<Double> list = pssm.get(aa);
		if( pos < 0 || pos >= groups || list == null )
			throw new PssmException("Invalid PSSM position");
		return list.get(pos);
	}
	
	public int getGroups() {
		return groups;
	}
	
	private Map<Aminoacid, List<Double>> pssm = new HashMap<>();
	private int groups = 0;	
}
