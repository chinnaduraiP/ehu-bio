package es.ehubio.proteomics.io;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.HeaderParser;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Ptm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.Spectrum;
import es.ehubio.proteomics.Spectrum.Peak;
import es.ehubio.proteomics.pipeline.Filter;

public class ProteomeDiscovererMsf extends MsMsFile {
	private final static Logger logger = Logger.getLogger(ProteomeDiscovererMsf.class.getName());
	
	@Override
	public MsMsData load(String path) throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection con = DriverManager.getConnection("jdbc:sqlite:"+path);
		logger.info("Connected to MSF file using SQLite");
				 			
		Map<Integer,Spectrum> spectra = loadSpectra(con);
		Map<Integer,Ptm> ptms = loadPtmTypes(con);
		Map<Integer,Peptide> peptides = loadPeptides(con, ptms, spectra);
		Map<Integer,Protein> proteins = loadProteins(con);
		loadRelations(con,peptides,proteins);		
		
		MsMsData data = new MsMsData();
		data.loadFromSpectra(spectra.values());
		
		Filter filter = new Filter(data);
		filter.run();
		
		loadPeaks(con,data.getSpectra());
		
		con.close();
		
		return data;
	}

	private void loadPeaks(Connection con, Set<Spectrum> spectra) throws SQLException {
		for( Spectrum spectrum : spectra ) {
			Statement statement = con.createStatement();
			ResultSet peaks = statement.executeQuery(String.format(
					"SELECT * FROM MassPeaks WHERE ScanNumbers=\"%s\";", spectrum.getScan()));
			while( peaks.next() ) {
				Peak peak = new Peak();
				peak.setIntensity(peaks.getDouble("Intensity"));
				peak.setCharge(peaks.getInt("Charge"));
				peak.setMz(peaks.getDouble("Mass")/peak.getCharge());
			}
		}
		
	}

	private void loadRelations(Connection con, Map<Integer, Peptide> peptides, Map<Integer, Protein> proteins) throws SQLException {
		for( Entry<Integer, Peptide> entry : peptides.entrySet() ) {
			Peptide peptide = entry.getValue();
			List<Integer> proteinIds = getProteins(con, entry.getKey(), peptide.getDecoy());
			/*if( proteinIds.isEmpty() )
				System.out.println(String.format("%s (%s)", entry.getKey(), peptide.getDecoy()));*/
			for( int proteinId : proteinIds ) {
				Protein protein = proteins.get(proteinId);
				protein.addPeptide(peptide);
			}
		}
	}
	
	private List<Integer> getProteins(Connection con, int peptide, boolean decoy ) throws SQLException {
		List<Integer> result = new ArrayList<>();
		Statement statement = con.createStatement();
		ResultSet relations = statement.executeQuery(String.format(
			"SELECT ProteinID FROM PeptidesProteins%s WHERE PeptideID=%d;", decoy?"_decoy":"", peptide));
		while( relations.next() )
			result.add(relations.getInt(1));
		return result;
	}

	private Map<Integer, Ptm> loadPtmTypes(Connection con) throws SQLException {
		Map<Integer, Ptm> result = new HashMap<>();
		Statement statement = con.createStatement();
		ResultSet ptms = statement.executeQuery("SELECT * FROM AminoAcidModifications;");
		while( ptms.next() ) {
			Ptm ptm = new Ptm();
			ptm.setMassDelta(ptms.getDouble("DeltaMass"));
			ptm.setName(ptms.getString("ModificationName"));
			result.put(ptms.getInt("AminoAcidModificationID"), ptm);
		}
		return result;
	}

	private Map<Integer,Spectrum> loadSpectra(Connection con) throws SQLException {
		Map<Integer,Spectrum> result = new HashMap<>();
		Statement statement = con.createStatement();
		ResultSet fileInfos;
		try {
			fileInfos = statement.executeQuery("SELECT FileName FROM SpectrumFileInfos;");
		} catch( SQLException e ) {
			fileInfos = statement.executeQuery("SELECT FileName FROM FileInfos;");
		}
		fileInfos.next();
		String fileName = fileInfos.getString(1);
		statement = con.createStatement();
		ResultSet spectra = statement.executeQuery("SELECT * FROM SpectrumHeaders;");
		while( spectra.next() ) {
			Spectrum spectrum = new Spectrum();
			spectrum.setFileName(fileName);
			spectrum.setScan(spectra.getInt("FirstScan")+"");
			spectrum.setFileId(spectrum.getScan());
			spectrum.setRt(spectra.getDouble("RetentionTime"));
			result.put(spectra.getInt("SpectrumID"), spectrum);
		}
		return result;
	}

	private Map<Integer,Peptide> loadPeptides(Connection con, Map<Integer, Ptm> ptmTypes, Map<Integer,Spectrum> spectra) throws SQLException {
		Map<Integer,Peptide> target = loadPeptides(con,false,ptmTypes,spectra); 
		/*Map<Integer,Peptide> decoy = loadPeptides(con,true,ptmTypes,spectra); 
		target.putAll(decoy);*/
		return target;
	}

	private Map<Integer, Peptide> loadPeptides(Connection con, boolean decoy, Map<Integer, Ptm> ptmTypes, Map<Integer,Spectrum> spectra) throws SQLException {
		Map<Integer,Peptide> result = new HashMap<>();
		Map<String,Peptide> mapPeptides = new HashMap<>();
		Statement statement = con.createStatement();
		ResultSet scores = statement.executeQuery("SELECT ScoreID FROM ProcessingNodeScores WHERE ScoreName=\"XCorr\";");
		scores.next();
		int xcorr = scores.getInt(1);
		statement = con.createStatement();
		ResultSet entries = statement.executeQuery(String.format("SELECT * FROM Peptides%s;", decoy?"_decoy":""));
		while( entries.next() ) {
			int id = entries.getInt("PeptideID");
			Peptide newPeptide = new Peptide();			
			newPeptide.setSequence(entries.getString("Sequence"));
			newPeptide.setDecoy(decoy);
			newPeptide.getPtms().addAll(loadPtms(con,id,newPeptide.getSequence(),decoy,ptmTypes));
			String idStr = newPeptide.getUniqueString();
			Peptide peptide = mapPeptides.get(idStr);
			if( peptide == null ) {
				peptide = newPeptide;
				result.put(id, peptide);
				mapPeptides.put(idStr, peptide);
			}
			Spectrum spectrum = spectra.get(entries.getInt("SpectrumID"));
			Psm psm = new Psm();
			psm.linkSpectrum(spectrum);
			psm.linkPeptide(peptide);
			psm.setRank(entries.getInt("SearchEngineRank"));
			psm.addScore(new Score(ScoreType.SEQUEST_XCORR, loadXcorr(con, xcorr, id,decoy)));
		}
		return result;
	}

	private double loadXcorr(Connection con, int xcorr, int peptide, boolean decoy) throws SQLException {
		Statement statement = con.createStatement();
		ResultSet scores = statement.executeQuery(String.format(
			"SELECT ScoreValue FROM PeptideScores%s WHERE PeptideID=%d AND ScoreID=%d;", decoy?"_decoy":"", peptide, xcorr));
		scores.next();
		return scores.getDouble(1);
	}

	private List<Ptm> loadPtms(Connection con, int peptide, String seq, boolean decoy, Map<Integer,Ptm> ptmTypes) throws SQLException {
		List<Ptm> result = new ArrayList<>();
		Statement statement = con.createStatement();
		ResultSet ptms = statement.executeQuery(String.format(
			"SELECT * FROM PeptidesAminoAcidModifications%s WHERE PeptideID=%d;", decoy?"_decoy":"", peptide));
		while( ptms.next() ) {
			Ptm ptm = new Ptm();
			Ptm type = ptmTypes.get(ptms.getInt("AminoAcidModificationID"));
			ptm.setMassDelta(type.getMassDelta());
			ptm.setName(type.getName());
			ptm.setPosition(ptms.getInt("Position")+1);
			ptm.setResidues(seq.charAt(ptm.getPosition()-1)+"");
			result.add(ptm);
		}
		return result;
	}

	private Map<Integer,Protein> loadProteins(Connection con) throws SQLException {
		Map<Integer,Protein> result = new HashMap<>();
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
			result.put(id, protein);
		}		
		return result;
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
