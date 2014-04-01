package es.ehubio.wregex.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import es.ehubio.cosmic.Loci;
import es.ehubio.cosmic.Locus;
import es.ehubio.db.Aminoacid;
import es.ehubio.dbptm.ProteinPtms;
import es.ehubio.dbptm.Ptm;
import es.ehubio.wregex.InputGroup;
import es.ehubio.wregex.Pssm;
import es.ehubio.wregex.PssmBuilder.PssmBuilderException;
import es.ehubio.wregex.ResultGroup;
import es.ehubio.wregex.Wregex;

public class Services {
	private final ExternalContext context;
	
	public Services( ExternalContext context ) {
		this.context = context;
	}
	
	public static List<ResultGroupEx> search(
			Wregex wregex, MotifInformation motif, List<InputGroup> inputGroups, boolean assayScores, long tout ) throws Exception {
		List<ResultGroupEx> resultGroupsEx = new ArrayList<>();
		List<ResultGroup> resultGroups;
		ResultGroupEx resultGroupEx;
		long wdt = System.currentTimeMillis() + tout;
		for( InputGroup inputGroup : inputGroups ) {
			//System.out.println(inputGroup.getHeader());
			if( assayScores )
				resultGroups = wregex.searchGroupingAssay(inputGroup);
			else
				resultGroups = wregex.searchGrouping(inputGroup.getFasta());
			for( ResultGroup resultGroup : resultGroups ) {
				resultGroupEx = new ResultGroupEx(resultGroup);
				if( motif != null ) {
					resultGroupEx.setMotif(motif.getName());
					resultGroupEx.setMotifUrl(motif.getReferences().get(0).getLink());
				}
				resultGroupsEx.add(resultGroupEx);
			}
			if( tout > 0 && System.currentTimeMillis() >= wdt )
				throw new Exception("Too intensive search, try a more strict regular expression or a smaller fasta file");
		}
		return resultGroupsEx;
	}
	
	public List<ResultGroupEx> searchAll(
			List<MotifInformation> motifs, List<InputGroup> inputGroups, long tout ) throws Exception {
		List<ResultGroupEx> results = new ArrayList<>();
		MotifDefinition def;
		Pssm pssm;
		Wregex wregex;
		for( MotifInformation motif : motifs ) {
			def = motif.getDefinitions().get(0);
			pssm = getPssm(def.getPssm());
			wregex = new Wregex(def.getRegex(), pssm);
			results.addAll(search(wregex, motif, inputGroups, false, tout));
		}
		return results;
	}
		
	public static List<ResultEx> expand(List<ResultGroupEx> resultGroups, boolean grouping) {
		List<ResultEx> results = new ArrayList<>();
		for( ResultGroupEx resultGroup : resultGroups ) {
			if( grouping )
				results.add(resultGroup.getRepresentative());
			else
				for( ResultEx r : resultGroup )
					results.add(r);
		}
		return results;
	}
	
	public static void searchCosmic(Map<String,Loci> cosmic, List<ResultEx> results) {
		int missense;
		boolean invalid;
		for( ResultEx result : results ) {
			Loci loci = cosmic.get(result.getGene());
			if( loci == null )
				continue;			
			missense = 0;
			invalid = false;
			for( Locus locus : loci.getLoci().values() ) {
				if( locus.position > result.getFasta().getSequence().length() ||
					locus.aa != Aminoacid.parseLetter(result.getFasta().getSequence().charAt(locus.position-1)) ) {
					invalid = true;
					break;
				}
				if( locus.position >= result.getStart() && locus.position <= result.getEnd() )
					missense += locus.mutations;
			}
			if( invalid ) {
				result.setCosmicUrl( String.format(
					"http://cancer.sanger.ac.uk/cosmic/gene/analysis?ln=%s&mut=%s",
					result.getGene(), "substitution_missense") );
				continue;
			}
			result.setCosmicUrl( String.format(
				"http://cancer.sanger.ac.uk/cosmic/gene/analysis?ln=%s&start=%d&end=%d&mut=%s",
				result.getGene(), result.getStart(), result.getEnd(), "substitution_missense") );
			result.setCosmicMissense(missense);
		}
	}
	
	public static void searchDbPtm(Map<String, ProteinPtms> dbPtm, List<ResultEx> results) {
		int count;
		for( ResultEx result : results ) {
			ProteinPtms ptms = dbPtm.get(result.getAccession());
			if( ptms == null )
				continue;
			count = 0;
			for( Ptm ptm : ptms.getPtms().values() )
				if( ptm.position >= result.getStart() && ptm.position <= result.getEnd() )
					count += ptm.count;
			result.setDbPtmUrl(String.format(
				"http://dbptm.mbc.nctu.edu.tw/search_result.php?search_type=db_id&swiss_id=%s",ptms.getId()));
			result.setDbPtms(count);			
		}
	}
	
	public Reader getResourceReader( String resource ) {
		return new InputStreamReader(context.getResourceAsStream("/resources/"+resource));
	}
	
	public Pssm getPssm( String name ) throws IOException, PssmBuilderException {
		if( name == null )
			return null;
		Reader rd = getResourceReader("data/"+name);
		Pssm pssm = Pssm.load(rd, true);
		rd.close();
		return pssm;
	}
	
	public long getInitNumber( String param ) {
		return Long.parseLong(context.getInitParameter(param));
	}
}
