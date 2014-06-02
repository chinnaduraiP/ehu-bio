package es.ehubio.tools;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import es.ehubio.db.fasta.Fasta;
import es.ehubio.db.fasta.HeaderParser;
import es.ehubio.proteomics.PAnalyzer;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Ptm;
import es.ehubio.proteomics.Spectrum;
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

public final class PAnalyzerCli implements Command.Interface {
	private final Logger logger = Logger.getLogger(PAnalyzerCli.class.getName());

	@Override
	public String getUsage() {
		return "</path/file.mzid>";
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}
	
	public CVParamType getCVParam( String accession, List<AbstractParamType> params ) {
		for( AbstractParamType param : params ) {
			if( !CVParamType.class.isInstance(param) )
				continue;
			CVParamType cv = (CVParamType)param;
			if( cv.getAccession().equalsIgnoreCase(accession) )
				return cv;
		}
		return null;
	}

	@Override
	public void run(String[] args) throws Exception {
		// Load mzid
		logger.info("Loading mzid ...");
		JAXBContext jaxbContext = JAXBContext.newInstance(MzIdentML.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		MzIdentML mzid = (MzIdentML)unmarshaller.unmarshal(new File(args[0]));
		
		// Load Proteins
		logger.info("Loading proteins ...");
		Map<String, Protein> mapProteins = new HashMap<>();
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
		
		// Load Peptides
		logger.info("Loading peptides ...");
		Map<String, Peptide> mapPeptides = new HashMap<>();
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
		
		// Load peptide-protein relations
		logger.info("Loading relations ...");
		Map<String,Peptide> mapRelations = new HashMap<>();
		for( PeptideEvidenceType peptideEvidenceType : mzid.getSequenceCollection().getPeptideEvidences() ) {
			Peptide peptide = mapPeptides.get(peptideEvidenceType.getPeptideRef());
			Protein protein = mapProteins.get(peptideEvidenceType.getDBSequenceRef());
			protein.addPeptide(peptide);
			peptide.setDecoy(peptideEvidenceType.isIsDecoy());
			mapRelations.put(peptideEvidenceType.getId(), peptide);
		}
		
		// Load Spectra
		logger.info("Loading spectra ...");
		Set<Spectrum> spectra = new HashSet<>();
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
		
		// PAnalyzer
		logger.info("Running PAnalyzer ...");
		PAnalyzer pAnalyzer = new PAnalyzer();
		pAnalyzer.setSpectra(spectra);
		pAnalyzer.buildGroups();
		int conclusive = 0;
		int nonconclusive = 0;
		int indistinguishable = 0;
		int ambigous = 0;
		for( ProteinGroup group : pAnalyzer.getGroups() ) {			
			switch (group.getConfidence()) {
				case CONCLUSIVE:
					//System.out.println(group.firstProtein().getAccession());
					conclusive++;
					break;
				case NON_CONCLUSIVE:
					nonconclusive++;
					break;
				case INDISTINGUISABLE_GROUP:
					indistinguishable++;
					break;
				case AMBIGUOUS_GROUP:
					ambigous++;
					break;
			}
		}
		for( Protein protein : pAnalyzer.getProteins() )
			if( protein.getAccession().equals("P22626") ) {
				for( Peptide peptide : protein.getPeptides() ) {
					System.out.print(peptide.toString()+": ");
					for( Protein protein2 : peptide.getProteins() )
						System.out.print(protein2.getAccession()+" ");
					System.out.println();
				}
				System.out.println();
			}
		System.out.println(String.format("Groups: %d", pAnalyzer.getGroups().size()));
		System.out.println(String.format("Conclusive: %d, Non-Conclusive: %d, Indistiguishable: %d, Ambigous: %d",conclusive,nonconclusive,indistinguishable,ambigous));
		logger.info("done!");
	}

}