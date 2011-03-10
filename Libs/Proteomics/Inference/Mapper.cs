// $Id: Mapper.cs 8 2011-03-10 19:14:46Z gorka $
// 
// Mapper.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      Mapper.cs
//  
// Copyright (c) 2011 Gorka Prieto
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

using System;
using System.IO;
using System.Collections.Generic;
using System.Xml;
using EhuBio.Proteomics.Hupo.mzIdentML;

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Infers proteins from peptide identifications
/// </summary>
public class Mapper {
	/// <summary>
	/// Counts
	/// </summary>
	public struct StatsStruct {
		public int Peptides;
		public int Red, Yellow, Green;
		public int MaxProteins;
		public int Conclusive;
		public int NonConclusive;
		public int Groups, Grouped;
		public int IGroups, Indistinguisable;
		public int Filtered;
	}
	
	/// <summary>
	/// Approaches for using peptides from different runs
	/// </summary>
	public enum MultiRunMode { VOTE, MERGE };

	/// <summary>
	/// Default constructor
	/// </summary>
	public Mapper() {
		Proteins = new List<Protein>();
		Peptides = new List<Peptide>();
		m_Stats = new StatsStruct();
		m_Format = new System.Globalization.NumberFormatInfo();
		m_Format.NumberDecimalSeparator = ".";
		m_Run = 0;
	}
	
	/// <summary>
	/// Loads data from a peptide identification file
	/// </summary>
	/// <param name="xmlpath">
	/// A <see cref="System.String"/> with the file path. This can be a mzIdentML file or a Waters XML file.
	/// </param>
	/// <param name="logpath">
	/// A <see cref="System.String"/> with the txt file containing the peptide thresholds (only use with Waters XML files)
	/// </param>
	/// <param name="merge">
	/// A <see cref="System.Boolean"/> indicating wether merge the new file with the existing data
	/// </param>
	public void LoadData( string xmlpath, string logpath, bool merge ) {
		if( !merge || m_Run == 0 ) {
			Proteins.Clear();
			Peptides.Clear();
			m_pid = m_gid = 1;
			m_SortedProteins = new SortedList<string, Protein>();
			m_Run = 1;
		} else
			m_Run++;

		m_mzid = null;
		if( xmlpath.ToLower().Contains(".mzid") )
			Loadmzid( xmlpath );
		else
			LoadWaters( xmlpath, logpath );
	}
	
	/// <summary>
	/// Loader of Waters XML and TXT files
	/// </summary>
	private void LoadWaters( string xmlpath, string logpath ) {
		SortedList<int,string> SortedAccession = new SortedList<int, string>();
	
		XmlDocument doc = new XmlDocument();
		doc.Load( xmlpath );
		bool UseScores = (logpath == null ? false : true);
		if( UseScores )
			LoadThresholds( logpath );
		
		XmlNodeList proteins = doc.GetElementsByTagName( "PROTEIN" );
		foreach( XmlElement element in proteins ) {
			int id = int.Parse(element.GetAttribute("ID"));
			if( SortedAccession.ContainsKey(id) )
				continue;
			string acc = element.GetElementsByTagName("ACCESSION")[0].InnerText;
			SortedAccession.Add( id, acc );
			if( m_SortedProteins.ContainsKey(acc) )
				continue;
			string entry = element.GetElementsByTagName("ENTRY")[0].InnerText;
			string desc = element.GetElementsByTagName("DESCRIPTION")[0].InnerText.Replace('+',' ');
			string seq = element.GetElementsByTagName("SEQUENCE")[0].InnerText.ToUpper();
			Protein p = new Protein(m_pid++, entry, acc, desc, seq);
			Proteins.Add( p );
			m_SortedProteins.Add( acc, p );
		}
		
		SortedList<int,Peptide> SortedPeptides = new SortedList<int, Peptide>();
		XmlNodeList peptides = doc.GetElementsByTagName( "PEPTIDE" );
		foreach( XmlElement element in peptides ) {
			int id = int.Parse(element.GetAttribute("ID"));
			int pid = int.Parse(element.GetAttribute("PROT_ID"));
			int mid = 0;
			if( UseScores )
				mid = int.Parse(element.GetAttribute("QUERY_MASS_ID"));
			string seq = element.GetAttribute("SEQUENCE").ToUpper();
			Peptide f = new Peptide(id, seq);
			f.Runs.Add( m_Run );
			Protein p = m_SortedProteins[SortedAccession[pid]];
			p.Peptides.Add( f );
			f.Proteins.Add( p );
			if( !p.Sequence.Contains(f.Sequence) )
				throw new ApplicationException( "Inconsistent sequence data" );
			Peptides.Add( f );
			if( UseScores )
				SortedPeptides.Add(mid,f);
		}
		if( !UseScores )
			return;
		
		XmlNodeList scores = doc.GetElementsByTagName( "MASS_MATCH" );
		foreach( XmlElement element in scores ) {
			int id = int.Parse(element.GetAttribute("ID"));
			double score = double.Parse(element.GetAttribute("SCORE"), m_Format);
			SortedPeptides[id].Score = score;
		}
	}

	/// <summary>
	/// Loads peptide score thresholds from Waters TXT file
	/// </summary>
	private void LoadThresholds( string fpath ) {
		TextReader r = new StreamReader( fpath );
		string t = r.ReadLine();
		while( t != null ) {
			if( t.Contains("Red-Yellow") ) {
				string[] f;
				f = t.Split(new string[]{"Red-Yellow Threshold is ", "Red-Yellow Threshold = "}, StringSplitOptions.None);
				f = f[1].Split(new char[]{' '});
				Peptide.YellowTh = double.Parse(f[0], m_Format);
			}
			if( t.Contains("Yellow-Green") ) {
				string[] f;
				f = t.Split(new string[]{"Yellow-Green Threshold is ", "Yellow-Green Threshold = "}, StringSplitOptions.None);
				f = f[1].Split(new char[]{' '});
				Peptide.GreenTh = double.Parse(f[0], m_Format);
			}
			t = r.ReadLine();
		}
		r.Close();
	}
	
	/// <summary>
	/// Loads a mzIdentML file
	/// </summary>
	private void Loadmzid( string mzid ) {
		m_mzid = new mzidFile();
		m_mzid.Load( mzid );
		
		// Proteins
		SortedList<string,string> SortedAccession = new SortedList<string, string>();
		foreach( PSIPIanalysissearchDBSequenceType element in m_mzid.ListProteins ) {
			if( SortedAccession.ContainsKey(element.id) )
				continue;
			string acc = element.accession;
			SortedAccession.Add( element.id, acc );
			if( m_SortedProteins.ContainsKey(acc) )
				continue;
			FuGECommonOntologycvParamType cv;
			cv = FuGECommonOntologycvParamType.Find( "MS:1001352", element.cvParam );
			string entry = cv == null ? "" : cv.value;
			cv = FuGECommonOntologycvParamType.Find( "MS:1001088", element.cvParam );
			string desc = cv == null ? "" : cv.value;
			string seq = element.seq;//.ToUpper();
			Protein p = new Protein(m_pid++, entry, acc, desc, seq);
			p.DBRef = element.id;
			Proteins.Add( p );
			m_SortedProteins.Add( acc, p );
		}
		
		// Peptides
		SortedList<string,Peptide> SortedPeptides = new SortedList<string, Peptide>();
		int id = 1;
		foreach( PSIPIpolypeptidePeptideType element in m_mzid.ListPeptides ) {
			string seq = element.peptideSequence;//.ToUpper();
			Peptide f = new Peptide(id++, seq);
			f.Confidence = Peptide.ConfidenceType.PassThreshold; // It will be filtered later if neccessary
			SortedPeptides.Add( element.id, f );
			f.Runs.Add( m_Run );
			Peptides.Add( f );
		}
		
		// Relations
		if( m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList.Length != 1 )
			throw new ApplicationException( "Multiple spectrum identification lists not supported" );
		foreach( PSIPIanalysissearchSpectrumIdentificationResultType idres in
			m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList[0].SpectrumIdentificationResult )
			foreach( PSIPIanalysissearchSpectrumIdentificationItemType item in idres.SpectrumIdentificationItem ) {
				if( !item.passThreshold )
					continue;
				Peptide f = SortedPeptides[item.Peptide_ref];
				if( item.PeptideEvidence == null )
					continue;
				f.Confidence = Peptide.ConfidenceType.PassThreshold;
				foreach( PSIPIanalysisprocessPeptideEvidenceType relation in item.PeptideEvidence ) {
					Protein p = m_SortedProteins[SortedAccession[relation.DBSequence_Ref]];
					if( f.Proteins.Contains(p) )
						continue;
					f.Names.Add( relation.DBSequence_Ref, relation.id );
					p.Peptides.Add( f );
					f.Proteins.Add( p );
				}
			}
	}
	
	/// <summary>
	/// Saves results to both a CSV and a mzIdentML file
	/// </summary>
	public void Save( string fpath ) {
		SaveCSV( Path.ChangeExtension(fpath,".csv"), ": " );
		SaveMzid( Path.ChangeExtension(fpath,".mzid"), null, null, null, null );
		//SaveMzid( Path.ChangeExtension(fpath,".mzid"), "SGIKER", "Proteomics Core Facility-SGIKER", "Dr. Kerman Aloria", "kerman.aloria@ehu.es" );
	}
	
	/// <summary>
	/// Save results to a CSV file
	/// </summary>
	public void SaveCSV( string fpath, string sep ) {
		TextWriter w = new StreamWriter( fpath );
		w.WriteLine( "ID"+sep+"Entry"+sep+"Accession"+sep+"Evidence"+sep+"Group"+sep+"Description"+sep+"Peptides"+sep+"Sequence" );
		foreach( Protein p in Proteins ) {
			if( p.Subset.Count == 0 )
				SaveCSVEntry( w, p, "", sep );
			else
				foreach( Protein p2 in p.Subset )
					SaveCSVEntry( w, p2, p.Entry, sep );
		}
		w.Close();
	}
	
	private void SaveCSVEntry( TextWriter w, Protein p, string grp, string sep ) {
		w.Write( p.ID + sep + p.Entry + sep + p.Accession + sep + p.Evidence.ToString() + sep + grp + sep + p.Desc + sep );
		foreach( Peptide f in p.Peptides )
			w.Write(f.ToString() + ' ');
		w.WriteLine( sep + p.Sequence );
	}
	
	/// <summary>
	/// Save results to a mzIdentML file
	/// </summary>
	public void SaveMzid(
		string mzid,
		string org_id, string org_name,
		string owner_name, string owner_email ) {
		// Previous file is required for including MS data
		if( m_mzid == null )
			return;
		
		// Header
		//m_mzid.AddOntology( "PSI-MS", "Proteomics Standards Initiative Mass Spectrometry Vocabularies", "2.25.0",
        //    "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo" );
        m_mzid.AddAnalysisSoftware(
            "PAnalyzer", "UPV/EHU Protein Inference", "0.1", "http://www.ehu.es", "UPV_EHU",
            "MS:1001267", "software vendor", "PSI-MS",
            "No customizations" );
        m_mzid.AddOrganization( "UPV_EHU", "University of the Basque Country",
            "Barrio Sarriena s/n, 48940 Leioa, Spain", "+34 94 601 200", "secretariageneral@ehu.es" );
        if( org_id != null ) {
        	m_mzid.SetProvider( org_id, "DOC_OWNER", "MS:1001271", "researcher", "PSI-MS" );
        	m_mzid.AddPerson( "DOC_OWNER", owner_name, owner_email, org_id );
        	m_mzid.AddOrganization( org_id, org_name );
        }
        
        // Analysis
        List<PSIPIanalysisprocessProteinAmbiguityGroupType> listGroup =
        	new List<PSIPIanalysisprocessProteinAmbiguityGroupType>();
        int hit = 1;
        foreach( Protein p in Proteins ) {
        	PSIPIanalysisprocessProteinAmbiguityGroupType grp = new PSIPIanalysisprocessProteinAmbiguityGroupType();
        	grp.id = "PAG_hit_" + (hit++);
        	int num = (p.Subset.Count == 0 ? 1 : p.Subset.Count);
        	grp.ProteinDetectionHypothesis = new PSIPIanalysisprocessProteinDetectionHypothesisType[num];
        	if( p.Subset.Count == 0 )
        		grp.ProteinDetectionHypothesis[0] = BuildHypothesis( p );
        	else {
        		int i = 0;
        		foreach( Protein p2 in p.Subset )
        			grp.ProteinDetectionHypothesis[i++] = BuildHypothesis( p2 );
        	}
        	listGroup.Add( grp );
        }
        PSIPIanalysisprocessProteinDetectionListType analysis = new PSIPIanalysisprocessProteinDetectionListType();
        analysis.id = "PAnalyzer_PDL";
       	analysis.ProteinAmbiguityGroup = listGroup.ToArray();
        m_mzid.Data.DataCollection.AnalysisData.ProteinDetectionList = analysis;
        
        // Save
        m_mzid.Save( mzid );
	}
	
	/// <summary>
	/// Builds a PDH for the current protein
	/// </summary>
	private PSIPIanalysisprocessProteinDetectionHypothesisType BuildHypothesis( Protein p ) {
		PSIPIanalysisprocessProteinDetectionHypothesisType h = new PSIPIanalysisprocessProteinDetectionHypothesisType();
		h.id = "PDH_" + p.Accession;
		h.DBSequence_ref = p.DBRef;
		if( p.Evidence == Protein.EvidenceType.NonConclusive || p.Evidence == Protein.EvidenceType.Filtered )
			h.passThreshold = false;
		else
			h.passThreshold = true;
		List<PeptideHypothesisType> listPeptides = new List<PeptideHypothesisType>();
		foreach( Peptide f in p.Peptides ) {
			PeptideHypothesisType peptide = new PeptideHypothesisType();
			peptide.PeptideEvidence_Ref = f.Names[p.DBRef];
			listPeptides.Add( peptide );
		}
		if( listPeptides.Count > 0 )
			h.PeptideHypothesis = listPeptides.ToArray();
		return h;
	}
	
	/// <summary>
	/// Process the loaded data
	/// </summary>
	/// <param name="th">
	/// A <see cref="Peptide.ConfidenceType"/> indicating a threshold for peptide filtering
	/// </param>
	/// <param name="mode">
	/// A <see cref="MultiRunMode"/> indicating how to merge peptides from different runs
	/// </param>
	public void Do( Peptide.ConfidenceType th, MultiRunMode mode ) {
		m_RunsTh = m_Run/2+1;
		m_Run = 0;
		FilterPeptides( th, mode );
		ClasifyPeptides();
		ClasifyProteins();
		DoStats();
	}
	
	/// <summary>
	/// Removes peptides with low score, duplicated (same sequence) or not voted (multirun)
	/// </summary>
	private void FilterPeptides( Peptide.ConfidenceType th, MultiRunMode mode ) {
		List<Peptide> peptides = new List<Peptide>();
		int id = 1;
		
		// Remove previous relations
		foreach( Protein p in Proteins )
			p.Peptides.Clear();
		
		// Filters duplicated (same sequence) peptides
		SortedList<string,Peptide> SortedPeptides = new SortedList<string, Peptide>();
		foreach( Peptide f in Peptides ) {
			// Low score peptide
			if( (int)f.Confidence < (int)th || f.Proteins.Count == 0 )
				continue;
			// Duplicated peptide, new protein?
			if( SortedPeptides.ContainsKey(f.Sequence) ) {
				Peptide fo = SortedPeptides[f.Sequence];
				if( (int)f.Confidence > (int)fo.Confidence )
					fo.Confidence = f.Confidence;
				if( !fo.Runs.Contains(f.Runs[0]) )
					fo.Runs.Add(f.Runs[0]);
				bool dp = false;	// duplicated protein?
				foreach( Protein po in fo.Proteins )
					if( po.ID == f.Proteins[0].ID ) {
						dp = true;
						break;
					}
				if( !dp )
					fo.Proteins.Add( f.Proteins[0] );
			// New peptide
			} else {
				f.ID = id++;
				SortedPeptides.Add( f.Sequence, f );
				peptides.Add( f );
			}
		}
		
		// Vote peptides
		if( mode == Mapper.MultiRunMode.VOTE ) {
			Peptides = new List<Peptide>();
			foreach( Peptide f in peptides )
				if( f.Runs.Count >= m_RunsTh )
					Peptides.Add(f);
		} else
			Peptides = peptides;
		
		// Asigns new peptides to proteins
		foreach( Peptide f in Peptides )
			foreach( Protein p in f.Proteins )
				p.Peptides.Add(f);
	}
	
	/// <summary>
	/// Peptide classifications
	/// </summary>
	private void ClasifyPeptides() {
		// 1. Locate unique peptides
		foreach( Peptide f in Peptides )
			if( f.Proteins.Count == 1 ) {
				f.Relation = Peptide.RelationType.Unique;
				f.Proteins[0].Evidence = Protein.EvidenceType.Conclusive;
			}
			else
				f.Relation = Peptide.RelationType.Meaningful;
		
		// 2. Locate non-meaningful peptides (first round)
		foreach( Protein p in Proteins )
			if( p.Evidence == Protein.EvidenceType.Conclusive )
				foreach( Peptide f in p.Peptides )
					if( f.Relation != Peptide.RelationType.Unique )
						f.Relation = Peptide.RelationType.Meaningless;
		
		// 3. Locate non-meaningful peptides (second round)
		foreach( Peptide f in Peptides ) {
			if( f.Relation != Peptide.RelationType.Meaningful )
				continue;
			foreach( Peptide f2 in f.Proteins[0].Peptides ) {
				if( f2.Relation == Peptide.RelationType.Meaningless )
					continue;
				if( f2.Proteins.Count <= f.Proteins.Count )
					continue;
				bool is_shared = false;
				foreach( Protein p in f.Proteins )
					if( !p.HasPeptide(f2) ) {
						is_shared = true;
						break;
					}
				if( !is_shared )
					f2.Relation = Peptide.RelationType.Meaningless;
			}
		}
	}
	
	/// <summary>
	/// Classifies proteins according to their peptides
	/// </summary>
	private void ClasifyProteins() {
		List<Protein> proteins = new List<Protein>();
		int id = 1;
				
		foreach( Protein p in Proteins )
			// Conclusive proteins
			if( p.Evidence == Protein.EvidenceType.Conclusive ) {
				p.ID = id++;
				proteins.Add( p );
			} else {
				bool is_group = false;
				foreach( Peptide f in p.Peptides )
					if( f.Relation == Peptide.RelationType.Meaningful ) {
						is_group = true;
						break;
					}
				// Group
				if( is_group )
					p.Evidence = Protein.EvidenceType.Group;
				// Non conclusive
				else {
					p.Evidence = Protein.EvidenceType.NonConclusive;
					p.ID = id++;
					proteins.Add( p );
				}
			}
				
		// Group proteins
		foreach( Protein p in Proteins )
			if( p.Evidence == Protein.EvidenceType.Group )
				AddToGroup( p, ref id, ref proteins );
		
		// Filtered and Undistinguisable
		foreach( Protein p in proteins )
			if( p.Subset.Count > 0 ) {
				if( IsIndistinguisable(p) ) {
					p.Evidence = Protein.EvidenceType.Indistinguishable;
					foreach( Protein p2 in p.Subset )
						p2.Evidence = Protein.EvidenceType.Indistinguishable;
				}
			} else if( p.Peptides.Count == 0 )
				p.Evidence = Protein.EvidenceType.Filtered;
		
		Proteins = proteins;
	}
	
	/// <summary>
	/// Includes a protein in a group
	/// </summary>
	private void AddToGroup( Protein p, ref int id, ref List<Protein> proteins ) {
		Protein g;
		
		if( p.Group == null ) {
			g = new Protein( id++, "Group " + (m_gid++), "", p.Name, "" );
			g.Evidence = Protein.EvidenceType.Group;
			p.Group = g;
			p.ID = id++;
			g.Subset.Add( p );
			proteins.Add( g );
		} else
			g = p.Group;
		
		foreach( Peptide f in p.Peptides ) {
			if( f.Relation != Peptide.RelationType.Meaningful )
				continue;
			foreach( Protein t in f.Proteins )
				if( t.Evidence == Protein.EvidenceType.Group && t.Group == null ) {
					t.ID = id++;
					t.Group = g;
					g.Subset.Add( t );
					g.Desc += " + " + t.Name;
				}
		}
	}
	
	private bool IsIndistinguisable( Protein g ) {
		foreach( Peptide f in g.Subset[0].Peptides ) {
			if( f.Relation != Peptide.RelationType.Meaningful )
				continue;
			foreach( Protein p in g.Subset )
				if( !p.HasPeptide(f) )
					return false;
		}
		return true;
	}
	
	private void DoStats() {
		// Peptides
		m_Stats.Peptides = Peptides.Count;
		m_Stats.Red = m_Stats.Yellow = m_Stats.Green = 0;
		foreach( Peptide f in Peptides )
			switch( f.Confidence ) {
				case Peptide.ConfidenceType.Red:
					m_Stats.Red++;
					break;
				case Peptide.ConfidenceType.Yellow:
					m_Stats.Yellow++;
					break;
				case Peptide.ConfidenceType.Green:
					m_Stats.Green++;
					break;
			}
		
		// Proteins
		m_Stats.MaxProteins = 0;
		m_Stats.Conclusive = 0;
		m_Stats.NonConclusive = 0;
		m_Stats.Groups = m_Stats.Grouped = 0;
		m_Stats.IGroups = m_Stats.Indistinguisable = 0;
		m_Stats.Filtered = 0;
		foreach( Protein p in Proteins )
			switch( p.Evidence ) {
				case Protein.EvidenceType.Conclusive:
					m_Stats.Conclusive++;
					m_Stats.MaxProteins++;
					break;
				case Protein.EvidenceType.NonConclusive:
					m_Stats.NonConclusive++;
					m_Stats.MaxProteins++;
					break;
				case Protein.EvidenceType.Group:
					m_Stats.Groups++;
					m_Stats.Grouped += p.Subset.Count;
					m_Stats.MaxProteins += p.Subset.Count;
					break;
				case Protein.EvidenceType.Indistinguishable:
					m_Stats.IGroups++;
					m_Stats.Indistinguisable += p.Subset.Count;
					m_Stats.MaxProteins += p.Subset.Count;
					break;
				case Protein.EvidenceType.Filtered:
					m_Stats.Filtered++;
					break;
			}
		//m_Stats.MinProteins = m_Stats.Conclusive + m_Stats.Groups;
	}
	
	/// <summary>
	/// Returns counts
	/// </summary>
	public StatsStruct Stats {
		get { return m_Stats; }
	}
	
	/// <summary>
	/// Protein list
	/// </summary>
	public List<Protein> Proteins;
	
	/// <summary>
	/// Peptide list
	/// </summary>
	public List<Peptide> Peptides;
		
	private int m_pid, m_gid;
	private StatsStruct m_Stats;
	private System.Globalization.NumberFormatInfo m_Format;
	private SortedList<string,Protein> m_SortedProteins;
	private int m_RunsTh, m_Run;
	private mzidFile m_mzid;
}

} // namespace EhuBio.Proteomics.Inference