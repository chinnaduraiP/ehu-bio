package es.ehubio.proteomics.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import es.ehubio.proteomics.MsMsData;

public abstract class MsMsFile {
	private final Logger logger = Logger.getLogger(MsMsFile.class.getName());
	protected MsMsData data;
	
	public MsMsData load( String path, String decoyRegex ) throws Exception {
		logger.info(String.format("Loading '%s' ...", path));
		InputStream input = new FileInputStream(path);
		if( path.endsWith(".gz") )
			input = new GZIPInputStream(input);
		data = load(input, decoyRegex);
		input.close();
		return data;
	}
	
	public abstract MsMsData load( InputStream input, String decoyRegex ) throws Exception;	

	public void save( String path ) throws Exception {
		logger.info(String.format("Saving '%s' ...", path));
		OutputStream output = new FileOutputStream(path);
		if( path.endsWith(".gz") )
			output = new GZIPOutputStream(output);
		save(output);
		output.close();
	}
	
	public abstract void save( OutputStream output ) throws Exception;
	
	public void loadIons( String optionalPath ) throws Exception {		
	}
}
