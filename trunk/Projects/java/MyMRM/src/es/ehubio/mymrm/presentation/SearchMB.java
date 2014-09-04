package es.ehubio.mymrm.presentation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import es.ehubio.db.fasta.Fasta.InvalidSequenceException;
import es.ehubio.mymrm.data.Peptide;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.pipeline.Digester;
import es.ehubio.proteomics.pipeline.PAnalyzer;

@ManagedBean
@SessionScoped
public class SearchMB implements Serializable {
	private static final long serialVersionUID = 1L;
	private String peptideSequence;
	private List<PeptideBean> seqPeptides;
	private PrecursorBean precursor;
	private String proteinAccession;
	private String fastaFile;
	private Protein protein;
	private List<es.ehubio.proteomics.Peptide> proteinPeptides = new ArrayList<>();
	
	public String getPeptideSequence() {
		return peptideSequence;
	}
	
	public void setPeptideSequence(String peptide) {
		this.peptideSequence = peptide;
	}
	
	public void searchPeptide( DatabaseMB db ) {
		seqPeptides = new ArrayList<>();
		for( Peptide peptide : db.search(peptideSequence) ) {
			PeptideBean bean = new PeptideBean();
			bean.setEntity(peptide);
			seqPeptides.add(bean);
		}
	}
	
	public String searchProteinPeptide( DatabaseMB db, String seq ) {
		peptideSequence = seq;
		searchPeptide(db);
		return "peptide";
	}
	
	public void searchProtein() {
		this.protein = null;
		proteinPeptides.clear();
		try {
			Set<es.ehubio.proteomics.Peptide> peptides = Digester.digestDatabase(
				new File(DatabaseMB.getFastaDir(),fastaFile).getAbsolutePath(),
				Enzyme.TRYPSIN,
				7);
			MsMsData data = new MsMsData();
			data.loadFromPeptides(peptides);
			PAnalyzer pAnalyzer = new PAnalyzer(data);
			pAnalyzer.run();
			for( Protein protein : data.getProteins() )
				if( protein.getAccession().equalsIgnoreCase(proteinAccession) ) {
					this.protein = protein;
					break;
				}
			if( this.protein != null ) {
				proteinPeptides.addAll(protein.getPeptides());
				Collections.sort(proteinPeptides, new Comparator<es.ehubio.proteomics.Peptide>() {
					@Override
					public int compare(es.ehubio.proteomics.Peptide p1, es.ehubio.proteomics.Peptide p2) {
						int res = p1.getConfidence().getOrder() - p2.getConfidence().getOrder(); 
						if( res != 0 )
							return res;
						res = p2.getSequence().length()-p1.getSequence().length();
						return res;
					}
				});
			}
		} catch (IOException | InvalidSequenceException e) {
			e.printStackTrace();
		}
	}

	public List<PeptideBean> getSeqPeptides() {
		return seqPeptides;
	}

	public PrecursorBean getPrecursor() {
		return precursor;
	}
	
	public String showDetails( PrecursorBean bean, DatabaseMB db ) {
		this.precursor = bean;
		for( DetailsBean experiment : bean.getExperiments() ) {
			if( experiment.getFragments().isEmpty() )			
				experiment.getFragments().addAll(db.getFragments(experiment.getPrecursor().getId()));
			if( experiment.getScores().isEmpty() )
				experiment.getScores().addAll(db.getScores(experiment.getEvidence().getId()));
		}
		return "transitions";
	}

	public String getProteinAccession() {
		return proteinAccession;
	}

	public void setProteinAccession(String proteinAccession) {
		this.proteinAccession = proteinAccession;
	}

	public String getFastaFile() {
		return fastaFile;
	}

	public void setFastaFile(String fastaFile) {
		this.fastaFile = fastaFile;
	}

	public Protein getProtein() {
		return protein;
	}

	public List<es.ehubio.proteomics.Peptide> getProteinPeptides() {
		return proteinPeptides;
	}
}
