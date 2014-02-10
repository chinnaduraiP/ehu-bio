package es.ehubio.cosmic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class Cosmic {
	public void openTsvGz( String path ) throws FileNotFoundException, IOException {
		db = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
		String line = db.readLine();
		if( line == null )
			return;
		fieldNames = line.split("\\t");
	}
	
	public void closeDb() {
		try {
			db.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Entry nextEntry() throws IOException {
		String line = db.readLine();
		if( line == null )
			return null;
		
		String[] fields = line.split("\\t");
		Entry entry = new Entry();
		int i = 0;
		entry.setGeneName(fields[i++]);
		if( i < fields.length )
			entry.setHgncId(fields[i++]);
		if( i < fields.length )
			entry.setSample(fields[i++]);
		if( i < fields.length )
			entry.setPrimarySite(fields[i++]);
		if( i < fields.length )
			entry.setSiteSubtype1(fields[i++]);
		if( i < fields.length )
			entry.setPrimaryHistology(fields[i++]);
		if( i < fields.length )
			entry.setHistologySubtype1(fields[i++]);
		if( i < fields.length )
			entry.setMutationId(fields[i++]);
		if( i < fields.length )
			entry.setMutationCds(fields[i++]);
		if( i < fields.length )
			entry.setMutationAa(fields[i++]);
		if( i < fields.length )
			entry.setMutationDescription(fields[i++]);
		if( i < fields.length )
			entry.setMutationZygosity(fields[i++]);
		if( i < fields.length )
			entry.setMutationNcbi36GenomePosition(fields[i++]);
		if( i < fields.length )
			entry.setMutationGrch37GenomePosition(fields[i++]);
		if( i < fields.length )
			entry.setPubmedId(fields[i++]);
		
		return entry;
	}
	
	public String[] getFieldNames() {
		return fieldNames;
	}

	private BufferedReader db;
	private String[] fieldNames;
}
