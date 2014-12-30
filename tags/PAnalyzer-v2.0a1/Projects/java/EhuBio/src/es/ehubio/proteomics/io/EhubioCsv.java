package es.ehubio.proteomics.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import es.ehubio.io.CsvUtils;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.ScoreType;

public class EhubioCsv extends MsMsFile {
	private final static Logger logger = Logger.getLogger(EhubioCsv.class.getName());
	private final MsMsData data;
	private final static char SEP = '\t';
	private final static char INTER = ',';
	private ScoreType psmScoreType = ScoreType.XTANDEM_EVALUE;	

	public EhubioCsv( MsMsData data ) {
		this.data = data;
	}
	
	public ScoreType getPsmScoreType() {
		return psmScoreType;
	}

	public void setPsmScoreType(ScoreType psmScoreType) {
		this.psmScoreType = psmScoreType;
	}

	@Override
	public MsMsData load(InputStream input, String decoyRegex) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(OutputStream output) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(String dir) throws Exception {
		logger.info(String.format("Saving CSVs to '%s' ...", dir));
		savePsms(new File(dir,"psms.csv").getAbsolutePath());
		savePeptides(new File(dir,"peptides.csv").getAbsolutePath());
		saveProteins(new File(dir,"proteins.csv").getAbsolutePath());
		saveGroups(new File(dir,"groups.csv").getAbsolutePath());		
	}	

	private void savePsms( String path ) throws IOException {		
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"id","decoy","calcMz","expMz","charge","file","spectrum",
			psmScoreType.getName(),
			ScoreType.PSM_P_VALUE.getName(),
			ScoreType.PSM_LOCAL_FDR.getName(),
			ScoreType.PSM_Q_VALUE.getName(),
			ScoreType.PSM_FDR_SCORE.getName(),
			"passThreshold"
			));
		for( Psm psm : data.getPsms() )
			pw.println(CsvUtils.getCsv(SEP,
				psm.getId(), Boolean.TRUE.equals(psm.getDecoy()), psm.getCalcMz(), psm.getExpMz(), psm.getCharge(),
				psm.getSpectrum().getFileName(), psm.getSpectrum().getFileId(),
				psm.getScoreByType(psmScoreType),
				psm.getScoreByType(ScoreType.PSM_P_VALUE),
				psm.getScoreByType(ScoreType.PSM_LOCAL_FDR),
				psm.getScoreByType(ScoreType.PSM_Q_VALUE),
				psm.getScoreByType(ScoreType.PSM_FDR_SCORE),
				psm.isPassThreshold()
				));
		pw.close();
	}
	
	private void savePeptides(String path) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"id","decoy","confidence","sequence","ptms","psms",
			ScoreType.PEPTIDE_P_VALUE.getName(),
			ScoreType.PEPTIDE_LOCAL_FDR.getName(),
			ScoreType.PEPTIDE_Q_VALUE.getName(),
			ScoreType.PEPTIDE_FDR_SCORE.getName(),
			"passThreshold"
			));
		for( Peptide peptide : data.getPeptides() )
			pw.println(CsvUtils.getCsv(SEP,
				peptide.getId(), Boolean.TRUE.equals(peptide.getDecoy()), peptide.getConfidence(), peptide.getSequence(), peptide.getMassSequence(),
				CsvUtils.getCsv(INTER, peptide.getPsms().toArray()),
				peptide.getScoreByType(ScoreType.PEPTIDE_P_VALUE),
				peptide.getScoreByType(ScoreType.PEPTIDE_LOCAL_FDR),
				peptide.getScoreByType(ScoreType.PEPTIDE_Q_VALUE),
				peptide.getScoreByType(ScoreType.PEPTIDE_FDR_SCORE),
				peptide.isPassThreshold()
				));
		pw.close();
	}
	
	private void saveProteins(String path) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"accession","decoy","confidence","peptides",
			ScoreType.PROTEIN_P_VALUE.getName(),
			ScoreType.PROTEIN_LOCAL_FDR.getName(),
			ScoreType.PROTEIN_Q_VALUE.getName(),
			ScoreType.PROTEIN_FDR_SCORE.getName(),
			"passThreshold"
			));
		for( Protein protein : data.getProteins() )
			pw.println(CsvUtils.getCsv(SEP,
				protein.getAccession(), Boolean.TRUE.equals(protein.getDecoy()), protein.getConfidence(),
				CsvUtils.getCsv(INTER, protein.getPeptides().toArray()),
				protein.getScoreByType(ScoreType.PROTEIN_P_VALUE),
				protein.getScoreByType(ScoreType.PROTEIN_LOCAL_FDR),
				protein.getScoreByType(ScoreType.PROTEIN_Q_VALUE),
				protein.getScoreByType(ScoreType.PROTEIN_FDR_SCORE),
				protein.isPassThreshold()
				));
		pw.close();
	}

	private void saveGroups(String path) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"id","name","decoy","confidence","proteins",
			ScoreType.GROUP_P_VALUE.getName(),
			ScoreType.GROUP_LOCAL_FDR.getName(),
			ScoreType.GROUP_Q_VALUE.getName(),
			ScoreType.GROUP_FDR_SCORE.getName(),
			"passThreshold"
			));
		for( ProteinGroup group : data.getGroups() )
			pw.println(CsvUtils.getCsv(SEP,
				group.getId(), group.buildName(), Boolean.TRUE.equals(group.getDecoy()), group.getConfidence(),
				CsvUtils.getCsv(INTER, group.getProteins().toArray()),
				group.getScoreByType(ScoreType.GROUP_P_VALUE),
				group.getScoreByType(ScoreType.GROUP_LOCAL_FDR),
				group.getScoreByType(ScoreType.GROUP_Q_VALUE),
				group.getScoreByType(ScoreType.GROUP_FDR_SCORE),
				group.isPassThreshold()
				));
		pw.close();
	}

	@Override
	public String getFilenameExtension() {
		return "tsv";
	}
}