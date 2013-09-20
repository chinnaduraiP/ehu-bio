package es.ehu.grk.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class UnixCfgReader {
	BufferedReader mReader;
	
	public UnixCfgReader( Reader rd ) {
		mReader = new BufferedReader(rd);
	}
	
	public String readLine() throws IOException {
		String str = mReader.readLine();
		while( str != null ) {
			str = str.trim();
			if( str.isEmpty() || str.startsWith("#") )
				str = mReader.readLine();
			else
				break;
		}
		return str;
	}
	
	public void close() throws IOException {
		if( mReader != null )
			mReader.close();
	}
}
