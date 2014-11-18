package es.ehubio.proteomics.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import es.ehubio.proteomics.MsMsData;

public abstract class MsMsFile {
	private final static Logger logger = Logger.getLogger(MsMsFile.class.getName());
	private File originalFile;
	protected MsMsData data;
	
	public static MsMsFile autoDetect( String path ) throws Exception {
		MsMsFile result = null;
		for( MsMsFile file : getParsers() )
			if( file.checkSignature(path) ) {
				result = file;
				break;
			}
		if( result == null )
			logger.warning("File format not detected");
		else
			logger.info(String.format("Detected %s file format",result.getClass().getSimpleName()));
		return result;
	}
	
	private static List<MsMsFile> getParsers() {
		List<MsMsFile> list = new ArrayList<>();
		File dir = new File(MsMsFile.class.getResource("/es/ehubio/proteomics/io").getFile());
		for( String name : dir.list() ) {
			if( !name.endsWith(".class") )
				continue;
			try {
				Class<?> cls = Class.forName("es.ehubio.proteomics.io."+name.substring(0, name.length()-6));
				if( MsMsFile.class.isAssignableFrom(cls) )
					list.add((MsMsFile)cls.newInstance());
			} catch (Exception e) {
				continue;
			}			
		}		
		return list;
	}
	
	public static MsMsData autoLoad( String path ) throws Exception {
		MsMsFile file = autoDetect(path);
		if( file == null )
			return null;
		return file.load(path);
	}
	
	public MsMsData load( String path ) throws Exception {
		logger.info(String.format("Loading '%s' ...", path));
		InputStream input = new FileInputStream(path);
		if( path.endsWith(".gz") )
			input = new GZIPInputStream(input);
		data = load(input);
		input.close();
		originalFile = new File(path);
		return data;
	}	
	
	public abstract MsMsData load( InputStream input ) throws Exception;	

	public void save( String path ) throws Exception {
		File file = new File(path);
		if( file.isDirectory() )
			path = new File(file,originalFile.getName()).getAbsolutePath();
		if( path.indexOf('.') == -1 )
			path = String.format("%s.%s", path, getFilenameExtension());
		logger.info(String.format("Saving '%s' ...", path));
		OutputStream output = new FileOutputStream(path);
		if( path.endsWith(".gz") )
			output = new GZIPOutputStream(output);
		save(output);
		output.close();
	}
	
	public void save( OutputStream output ) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	public List<File> loadPeaks( String optionalPath ) throws Exception {
		return null;
	}
	
	public abstract String getFilenameExtension();
	
	public boolean checkSignature( String path ) throws Exception {
		InputStream input = new FileInputStream(path);
		if( path.endsWith(".gz") )
			input = new GZIPInputStream(input);
		boolean res = checkSignature(input);
		input.close();
		return res;
	}

	public boolean checkSignature(InputStream input) throws Exception {
		return false;
	}
}
