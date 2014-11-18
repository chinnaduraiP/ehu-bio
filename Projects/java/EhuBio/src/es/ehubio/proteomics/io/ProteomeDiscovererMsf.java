package es.ehubio.proteomics.io;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.HeaderParser;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Protein;

public class ProteomeDiscovererMsf extends MsMsFile {
	private final static Logger logger = Logger.getLogger(ProteomeDiscovererMsf.class.getName());
	
	@Override
	public MsMsData load(String path) throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection con = DriverManager.getConnection("jdbc:sqlite:"+path);
		logger.info("Connected to MSF file using SQLite");
				
		try {
			loadProteins(con);
		} catch( Exception e ) {
			e.printStackTrace();
		}
		con.close();
		
		return null;
	}

	private void loadProteins(Connection con) throws SQLException {
		Statement statement = con.createStatement();
		ResultSet proteins = statement.executeQuery("SELECT * FROM Proteins;");
		while( proteins.next() ) {
			Protein protein = new Protein();
			protein.setSequence(proteins.getString("Sequence"));
			int id = proteins.getInt("ProteinID");
			Statement statement2 = con.createStatement();
			ResultSet descriptions = statement2.executeQuery(String.format(
				"SELECT Description FROM ProteinAnnotations WHERE ProteinID=%d;", id));
			if( !descriptions.next() )
				protein.setAccession(""+id);
			else {
				String description = descriptions.getString(1);
				if( description.startsWith(">") )
					description = description.substring(1);
				HeaderParser parser = Fasta.guessParser(description);
				if( parser == null )
					protein.setAccession(description);
				else {
					protein.setAccession(parser.getAccession());
					protein.setDescription(parser.getDescription());
					protein.setName(parser.getProteinName());
				}
			}
		}		
	}

	@Override
	public MsMsData load(InputStream input) throws Exception {		
		return null;
	}
	
	@Override
	public boolean checkSignature(InputStream input) throws Exception {
		byte[] sig = new byte[SIG.length()];
		input.read(sig);
		String sigStr = new String(sig);
		return sigStr.equals(SIG);		
	}

	@Override
	public String getFilenameExtension() {
		return "msf";
	}

	private static final String SIG = "SQLite format";
}
