package es.ehubio.proteomics.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import es.ehubio.io.CsvUtils;
import es.ehubio.proteomics.DecoyBase;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.ProteinGroup;
import es.ehubio.proteomics.Psm;
import es.ehubio.proteomics.Score;
import es.ehubio.proteomics.ScoreType;

public class EhubioCsv extends MsMsFile {
	//private final static Logger logger = Logger.getLogger(EhubioCsv.class.getName());
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
	protected boolean savePath(String dir) throws Exception {
		File file = new File(dir);
		if( !file.isDirectory() )
			dir = file.getParent();
		//logger.info(String.format("Saving CSVs to '%s' ...", dir));
		savePsms(new File(dir,"psms.csv").getAbsolutePath());
		savePeptides(new File(dir,"peptides.csv").getAbsolutePath());
		saveProteins(new File(dir,"proteins.csv").getAbsolutePath());
		saveGroups(new File(dir,"groups.csv").getAbsolutePath());
		return true;
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
			ScoreType.LPS_SCORE.getName(),
			"passThreshold"
			));
		for( Psm psm : data.getPsms() )
			pw.println(CsvUtils.getCsv(SEP,
				psm.getId(), Boolean.TRUE.equals(psm.getDecoy()), psm.getCalcMz(), psm.getExpMz(), psm.getCharge(),
				psm.getSpectrum().getFileName(), psm.getSpectrum().getFileId(),
				getScore(psm,psmScoreType),
				getScore(psm,ScoreType.PSM_P_VALUE),
				getScore(psm,ScoreType.PSM_LOCAL_FDR),
				getScore(psm,ScoreType.PSM_Q_VALUE),
				getScore(psm,ScoreType.PSM_FDR_SCORE),
				getScore(psm,ScoreType.LPS_SCORE),
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
			ScoreType.LPP_SCORE.getName(),
			"passThreshold"
			));
		for( Peptide peptide : data.getPeptides() )
			pw.println(CsvUtils.getCsv(SEP,
				peptide.getId(), Boolean.TRUE.equals(peptide.getDecoy()), peptide.getConfidence(), peptide.getSequence(), peptide.getMassSequence(),
				CsvUtils.getCsv(INTER, peptide.getPsms().toArray()),
				getScore(peptide,ScoreType.PEPTIDE_P_VALUE),
				getScore(peptide,ScoreType.PEPTIDE_LOCAL_FDR),
				getScore(peptide,ScoreType.PEPTIDE_Q_VALUE),
				getScore(peptide,ScoreType.PEPTIDE_FDR_SCORE),
				getScore(peptide,ScoreType.LPP_SCORE),
				peptide.isPassThreshold()
				));
		pw.close();
	}
	
	private void saveProteins(String path) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"accession","decoy","confidence","peptides","#peptides",
			ScoreType.PROTEIN_P_VALUE.getName(),
			ScoreType.PROTEIN_LOCAL_FDR.getName(),
			ScoreType.PROTEIN_Q_VALUE.getName(),
			ScoreType.PROTEIN_FDR_SCORE.getName(),
			ScoreType.LPQCORR_SCORE.getName(),
			ScoreType.LPQ_SCORE.getName(),
			ScoreType.MQ_VALUE.getName(),
			"passThreshold"
			));
		for( Protein protein : data.getProteins() )
			pw.println(CsvUtils.getCsv(SEP,
				protein.getAccession(), Boolean.TRUE.equals(protein.getDecoy()), protein.getConfidence(),
				CsvUtils.getCsv(INTER, protein.getPeptides().toArray()),
				protein.getPeptides().size(),
				getScore(protein,ScoreType.PROTEIN_P_VALUE),
				getScore(protein,ScoreType.PROTEIN_LOCAL_FDR),
				getScore(protein,ScoreType.PROTEIN_Q_VALUE),
				getScore(protein,ScoreType.PROTEIN_FDR_SCORE),
				getScore(protein,ScoreType.LPQCORR_SCORE),
				getScore(protein,ScoreType.LPQ_SCORE),
				getScore(protein,ScoreType.MQ_VALUE),
				protein.isPassThreshold()
				));
		pw.close();
	}

	private void saveGroups(String path) throws IOException {
		PrintWriter pw = new PrintWriter(path);
		pw.println(CsvUtils.getCsv(SEP,
			"id","name","decoy","confidence","proteins","#proteins","peptides","#peptides",
			ScoreType.GROUP_P_VALUE.getName(),
			ScoreType.GROUP_LOCAL_FDR.getName(),
			ScoreType.GROUP_Q_VALUE.getName(),
			ScoreType.GROUP_FDR_SCORE.getName(),
			ScoreType.LPG_SCORE.getName(),
			"passThreshold"
			));
		for( ProteinGroup group : data.getGroups() )
			pw.println(CsvUtils.getCsv(SEP,
				group.getId(), group.buildName(), Boolean.TRUE.equals(group.getDecoy()), group.getConfidence(),
				CsvUtils.getCsv(INTER, group.getProteins().toArray()),
				group.getProteins().size(),
				CsvUtils.getCsv(INTER, group.getPeptides().toArray()),
				group.getPeptides().size(),
				getScore(group,ScoreType.GROUP_P_VALUE),
				getScore(group,ScoreType.GROUP_LOCAL_FDR),
				getScore(group,ScoreType.GROUP_Q_VALUE),
				getScore(group,ScoreType.GROUP_FDR_SCORE),
				getScore(group,ScoreType.LPG_SCORE),
				group.isPassThreshold()
				));
		pw.close();
	}
	
	private static Object getScore( DecoyBase item, ScoreType type ) {
		Score score = item.getScoreByType(type);
		if( score == null )
			return "";
		return score.getValue();
	}

	@Override
	public String getFilenameExtension() {
		return "tsv";
	}
}