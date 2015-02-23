package es.ehubio.proteomics.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.ehubio.model.Aminoacid;
import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.proteomics.Peptide;
import es.ehubio.proteomics.Protein;
import es.ehubio.proteomics.Ptm;

public class ConfigDetector {
	public static class Modification {		
		public Aminoacid getAa() {
			return aa;
		}
		public void setAa(Aminoacid aa) {
			this.aa = aa;
		}
		public double getDeltaMass() {
			return deltaMass;
		}
		public void setDeltaMass(double deltaMass) {
			this.deltaMass = deltaMass;
		}
		public boolean isFixed() {
			return fixed;
		}
		public void setFixed(boolean fixed) {
			this.fixed = fixed;
		}
		public boolean isCterm() {
			return cterm;
		}
		public void setCterm(boolean cterm) {
			this.cterm = cterm;
		}
		public boolean isNterm() {
			return nterm;
		}
		public void setNterm(boolean nterm) {
			this.nterm = nterm;
		}
		private Aminoacid aa = null;
		private double deltaMass = 0.0;
		private boolean fixed = false;
		private boolean cterm = false;
		private boolean nterm = false;
	}
	
	public ConfigDetector() {
		this(100,2);
	}
	
	public ConfigDetector( int proteinSubset, int maxMissedCleavages ) {
		this.proteinSubset = proteinSubset;
		this.maxMissedCleavages = maxMissedCleavages;
	}
	
	public int getMinPeptideLength( MsMsData data ) {
		int minPeptideLength = data.getPeptides().iterator().next().getSequence().length();
		for( Peptide peptide : data.getPeptides() ) {
			int len = peptide.getSequence().length();
			if( len < minPeptideLength )
				minPeptideLength = len;
		}
		return minPeptideLength;
	}
	
	public int getMaxPeptideLength( MsMsData data ) {
		int maxPeptideLength = data.getPeptides().iterator().next().getSequence().length();
		for( Peptide peptide : data.getPeptides() ) {
			int len = peptide.getSequence().length();
			if( len > maxPeptideLength )
				maxPeptideLength = len;
		}
		return maxPeptideLength;
	}
	
	public Enzyme getEnzyme( MsMsData data ) {
		Enzyme result = null;
		for( Enzyme enzyme : Enzyme.values() ) {
			int count = proteinSubset;
			for( Protein protein : data.getProteins() ) {
				List<String> peptides = new ArrayList<String>();
				for( String peptide : Digester.digestSequence(protein.getSequence(),enzyme,maxMissedCleavages) )
					peptides.add(peptide.toLowerCase());
				for( Peptide peptide : protein.getPeptides() )
					if( !peptides.contains(peptide.getSequence().toLowerCase()) ) {
						enzyme = null;
						break;
					}
				if( --count == 0 || enzyme == null )
					break;
			}
			if( enzyme != null ) {
				result = enzyme;
				break;
			}
		}
		return result;
	}
	
	public int getMissedCleavages(MsMsData data, Enzyme enzyme) {
		int missedCleavages = 0;
		int count = proteinSubset;
		for( Protein protein : data.getProteins() ) {
			List<String> peptides = new ArrayList<String>();
			for( String peptide : Digester.digestSequence(protein.getSequence(),enzyme,missedCleavages) )
				peptides.add(peptide.toLowerCase());
			for( Peptide peptide : protein.getPeptides() )
				if( !peptides.contains(peptide.getSequence().toLowerCase()) ) {
					missedCleavages++;
					break;
				}
			if( --count == 0 || missedCleavages > maxMissedCleavages )
				break;
		}
		return missedCleavages <= maxMissedCleavages ? missedCleavages : -1;
	}
	
	public int getMaxModsPerPeptide(MsMsData data, Aminoacid[] varMods) {
		if( varMods == null || varMods.length == 0 )
			return 0;
		
		Set<Character> chars = new HashSet<>();
		for( Aminoacid aa : varMods )
			chars.add(Character.toUpperCase(aa.letter));
		
		int max = 0;		
		for( Peptide peptide : data.getPeptides() ) {
			Map<Character, Integer> mapCount = new HashMap<>();
			for( Ptm ptm : peptide.getPtms() ) {
				if( ptm.getAminoacid() == null || !chars.contains(Character.toUpperCase(ptm.getAminoacid())) )
					continue;
				Integer count = mapCount.get(ptm.getAminoacid());
				if( count == null )
					mapCount.put(ptm.getAminoacid(), 1);
				else
					mapCount.put(ptm.getAminoacid(), count+1);
			}
			for( Integer count : mapCount.values() )
				if( count > max )
					max = count;
		}
		return max;
	}

	public List<Modification> getMods(MsMsData data) {
		List<Modification> mods = new ArrayList<>();
		for( Peptide peptide : data.getPeptides() )
			for( Ptm ptm : peptide.getPtms() ) {				
				if( ptm.getAminoacid() == null || ptm.getMassDelta() == null )
					continue;
				boolean add = true;
				for( Modification mod : mods )
					if( mod.getAa().letter == ptm.getAminoacid() && Math.round(mod.getDeltaMass()*100) == Math.round(ptm.getMassDelta()*100) ) {
						add = false;
						break;
					}
				if( add ) {
					Modification mod = new Modification();
					mod.setAa(Aminoacid.parseLetter(ptm.getAminoacid()));
					mod.setDeltaMass(ptm.getMassDelta());
					mods.add(mod);
				}
			}
		findVarMods(mods, data);
		//findNtermMods(mods, data);
		//findCtermMods(mods, data);
		return mods;
	}	

	private void findVarMods(List<Modification> mods, MsMsData data) {
		for( Modification mod : mods ) {
			mod.setFixed(true);
			for( Peptide peptide : data.getPeptides() )
				if( countChars(peptide.getSequence(), mod.getAa()) > peptide.getPtms().size() ) {
					mod.setFixed(false);
					break;
				}
		}
	}
	
	private int countChars( String seq, Aminoacid aa ) {
		char ch = Character.toUpperCase(aa.letter);
		char[] chars = seq.toUpperCase().toCharArray();
		int count = 0;
		for( int i = 0; i < chars.length; i++ )
			if( chars[i] == ch )
				count++;
		return count;
	}
	
	private final int proteinSubset;
	private final int maxMissedCleavages;	
}
