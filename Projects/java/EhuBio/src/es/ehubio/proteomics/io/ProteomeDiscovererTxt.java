package es.ehubio.proteomics.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehubio.io.CsvReader;
import es.ehubio.model.ProteinModificationType;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Ptm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.Spectrum;

public class ProteomeDiscovererTxt extends MsMsFile {
	@Override
	public boolean checkSignature(InputStream input) throws Exception {
		BufferedReader rd = new BufferedReader(new InputStreamReader(input));
		String str = rd.readLine();
		return str.contains("Annotated Sequence") && str.contains("Protein Accessions");
	}

	@Override
	public MsMsData load(InputStream input) throws Exception {
		Map<String,Spectrum> scans = new HashMap<>();
		Map<String,Peptide> peptides = new HashMap<>();
		Map<String,Protein> proteins = new HashMap<>();
		
		Reader rd = new InputStreamReader(input);
		CsvReader csv = new CsvReader("\t");
		csv.open(rd);
		while( csv.readLine() != null ) {
			String scan = csv.getField("First Scan");
			Spectrum spectrum = scans.get(scan);
			if( spectrum == null ) {
				spectrum = loadSpectrum(csv, scan);
				scans.put(scan, spectrum);
			}
			Peptide newPeptide = loadPeptide(csv);
			Peptide peptide = peptides.get(newPeptide.getUniqueString());
			if( peptide == null ) {
				peptides.put(newPeptide.getUniqueString(), newPeptide);
				peptide = newPeptide;
			}			
			Psm psm = loadPsm(csv);
			psm.linkSpectrum(spectrum);
			psm.linkPeptide(peptide);
			loadProteins(csv,proteins,peptide);
		}
		
		MsMsData data = new MsMsData();
		data.loadFromSpectra(scans.values());
		return data;
	}	

	private Spectrum loadSpectrum( CsvReader csv, String scan ) {
		Spectrum spectrum = new Spectrum();
		spectrum.setScan(scan);
		spectrum.setFileId(scan);
		spectrum.setFileName(csv.getField("Spectrum File"));
		spectrum.setRt(csv.getDoubleField("RT [min]"));
		return spectrum;
	}
	
	private Peptide loadPeptide(CsvReader csv) throws Exception {
		Peptide peptide = new Peptide();
		peptide.setSequence(csv.getField("Annotated Sequence"));
		String modString = csv.getField("Modifications");
		peptide.setUniqueString(peptide.getSequence()+modString);
		if( !modString.isEmpty() ) {
			String[] mods = modString.split("; ");
			for( String mod : mods ) {
				Ptm ptm = new Ptm();
				Matcher matcher = ptmPattern.matcher(mod);
				if( matcher.find() ) {
					ptm.setResidues(matcher.group(1));
					if( !matcher.group(2).isEmpty() )
						ptm.setPosition(Integer.parseInt(matcher.group(2)));
					ptm.setName(matcher.group(3));
					ProteinModificationType type = ProteinModificationType.getByName(ptm.getName());
					if( type != null ) { 
						ptm.setMassDelta(type.getMass());
						ptm.setType(type);
					}
				} else
					ptm.setName(mod);
				peptide.addPtm(ptm);
			}
		}
		return peptide;
	}
	
	private Psm loadPsm( CsvReader csv ) {
		Psm psm = new Psm();
		psm.setCharge(csv.getIntField("Charge"));
		psm.setExpMz(csv.getDoubleField("m/z [Da]"));
		psm.setRank(csv.getIntField("Rank"));
		Score score = new Score(ScoreType.SEQUEST_XCORR, csv.getDoubleField("XCorr"));
		psm.addScore(score);
		return psm;
	}
	
	private void loadProteins(CsvReader csv, Map<String, Protein> proteins, Peptide peptide) {
		String accString = csv.getField("Protein Accessions");
		String[] accs = accString.split("; ");
		for( String acc : accs ) {
			Protein protein = proteins.get(acc);
			if( protein == null ) {
				protein = new Protein();
				protein.setAccession(acc);
				proteins.put(acc, protein);
			}
			peptide.addProtein(protein);
		}
	}

	@Override
	public String getFilenameExtension() {
		return "txt";
	}

	private final Pattern ptmPattern = Pattern.compile("(\\D+)(\\d*)\\((.*)\\)");
}
