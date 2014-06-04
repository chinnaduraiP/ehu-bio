package es.ehubio.proteomics;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.HeaderParser;
import es.ehubio.proteomics.psi.mzid11.AbstractParamType;
import es.ehubio.proteomics.psi.mzid11.CVParamType;
import es.ehubio.proteomics.psi.mzid11.DBSequenceType;
import es.ehubio.proteomics.psi.mzid11.ModificationType;
import es.ehubio.proteomics.psi.mzid11.MzIdentML;
import es.ehubio.proteomics.psi.mzid11.PeptideEvidenceRefType;
import es.ehubio.proteomics.psi.mzid11.PeptideEvidenceType;
import es.ehubio.proteomics.psi.mzid11.PeptideType;
import es.ehubio.proteomics.psi.mzid11.SpectrumIdentificationItemType;
import es.ehubio.proteomics.psi.mzid11.SpectrumIdentificationResultType;

public final class Mzid {
	private final Logger logger = Logger.getLogger(Mzid.class.getName());
	private MzIdentML mzid;
	private Map<String,Protein> mapProteins = new HashMap<>();
	private Map<String,Peptide> mapPeptides = new HashMap<>();
	private Map<String,Peptide> mapRelations = new HashMap<>();
	private Set<Spectrum> spectra = new HashSet<>();
	private MsMsData data;
	
	public MsMsData load( String path ) throws IOException, JAXBException {
		logger.info(String.format("Loading '%s' ...", path));
		InputStream input = new FileInputStream(path);
		if( path.endsWith(".gz") )
			input = new GZIPInputStream(input);
		load(input);
		input.close();
		return data;
	}
	
	public MsMsData load( InputStream input ) throws JAXBException {
		logger.info("Parsing XML ...");
		JAXBContext jaxbContext = JAXBContext.newInstance(MzIdentML.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		mzid = (MzIdentML)unmarshaller.unmarshal(input);		
		loadProteins();
		loadPeptides();
		loadRelations();
		loadSpectra();
		logger.info("finished!");
		data = new MsMsData();
		data.loadFromSpectra(spectra);
		return data;
	}
	
	private void loadProteins() {
		logger.info("Building proteins ...");
		mapProteins.clear();
		for( DBSequenceType dbSequence : mzid.getSequenceCollection().getDBSequences() ) {			
			Protein protein = new Protein();
			mapProteins.put(dbSequence.getId(), protein);
			protein.setAccession(dbSequence.getAccession());
			HeaderParser header = Fasta.guessParser(dbSequence.getAccession());
			if( header != null ) {
				protein.setAccession(header.getAccession());
				protein.setName(header.getProteinName());
				protein.setDescription(header.getDescription());
			}
			if( dbSequence.getName() != null )
				protein.setName(dbSequence.getName());
			CVParamType cv = getCVParam("MS:1001088", dbSequence.getCvParamsAndUserParams());
			if( cv != null )
				protein.setDescription(cv.getValue());
			protein.setSequence(dbSequence.getSeq());			
		}
	}
	
	private void loadPeptides() {
		logger.info("Building peptides ...");
		mapPeptides.clear();
		for( PeptideType peptideType : mzid.getSequenceCollection().getPeptides() ) {
			Peptide peptide = new Peptide();
			mapPeptides.put(peptideType.getId(), peptide);
			peptide.setSequence(peptideType.getPeptideSequence());
			for( ModificationType modificationType : peptideType.getModifications() ) {
				Ptm ptm = new Ptm();
				StringBuilder builder = new StringBuilder();
				for( String residue : modificationType.getResidues() )
					builder.append(residue);
				ptm.setAminoacid(builder.toString());
				ptm.setPosition(modificationType.getLocation());
				for( CVParamType param : modificationType.getCvParams() )
					if( param.getCvRef().equalsIgnoreCase("UNIMOD") ) {
						ptm.setName(param.getName());
						break;
					}
				peptide.addPtm(ptm);
			}			
		}
	}
	
	private void loadRelations() {
		logger.info("Building protein-peptide map ...");
		mapRelations.clear();
		for( PeptideEvidenceType peptideEvidenceType : mzid.getSequenceCollection().getPeptideEvidences() ) {
			Peptide peptide = mapPeptides.get(peptideEvidenceType.getPeptideRef());
			Protein protein = mapProteins.get(peptideEvidenceType.getDBSequenceRef());
			protein.addPeptide(peptide);
			peptide.setDecoy(peptideEvidenceType.isIsDecoy());
			mapRelations.put(peptideEvidenceType.getId(), peptide);
		}
	}

	private void loadSpectra() {
		logger.info("Building spectra ...");
		spectra.clear();
		for( SpectrumIdentificationResultType sir : mzid.getDataCollection().getAnalysisData().getSpectrumIdentificationLists().get(0).getSpectrumIdentificationResults() ) {
			Spectrum spectrum = new Spectrum();			
			spectrum.setFileName(sir.getSpectraDataRef());
			spectrum.setFileId(sir.getSpectrumID());
			for( SpectrumIdentificationItemType sii : sir.getSpectrumIdentificationItems() ) {
				Psm psm = new Psm();
				psm.linkSpectrum(spectrum);
				psm.setCharge(sii.getChargeState());
				psm.setMz(sii.getExperimentalMassToCharge());
				psm.setRank(sii.getRank());
				loadScores(psm, sii);
				if( sii.getPeptideEvidenceReves() == null )
					continue;
				for( PeptideEvidenceRefType peptideEvidenceRefType : sii.getPeptideEvidenceReves() ) {
					Peptide peptide = mapRelations.get(peptideEvidenceRefType.getPeptideEvidenceRef());
					if( peptide != null ) {
						psm.linkPeptide(peptide);
						break;
					}
				}
			}
			spectra.add(spectrum);
		}
	}
	
	private void loadScores( Psm psm, SpectrumIdentificationItemType sii ) {
		psm.addScore(new Psm.Score(Psm.ScoreType.MZID_PASS_THRESHOLD,sii.isPassThreshold()?1.0:0.0));
		for( AbstractParamType param : sii.getCvParamsAndUserParams() ) {
			if( !CVParamType.class.isInstance(param) )
				continue;
			Psm.ScoreType type = null;
			CVParamType cv = (CVParamType)param;
			switch( cv.getAccession() ) {
				case "MS:1001155": type = Psm.ScoreType.SEQUEST_XCORR; break;
				case "MS:1001172": type = Psm.ScoreType.MASCOT_EVALUE; break;
				case "MS:1001171": type = Psm.ScoreType.MASCOT_SCORE; break;
				case "MS:1001330": type = Psm.ScoreType.XTANDEM_EVALUE; break;
			}
			if( type == null )
				continue;
			psm.addScore(new Psm.Score(type, cv.getName(), Double.parseDouble(cv.getValue())));
		}
	}
	
	private CVParamType getCVParam( String accession, List<AbstractParamType> params ) {
		for( AbstractParamType param : params ) {
			if( !CVParamType.class.isInstance(param) )
				continue;
			CVParamType cv = (CVParamType)param;
			if( cv.getAccession().equalsIgnoreCase(accession) )
				return cv;
		}
		return null;
	}
}
