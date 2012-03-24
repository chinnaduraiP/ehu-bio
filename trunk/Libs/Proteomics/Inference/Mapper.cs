// $Id$
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
using EhuBio.UI.Html;

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Infers proteins from peptide identifications
/// </summary>
public abstract class Mapper {
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
	/// Software information.
	/// </summary>
	public struct Software {	
		public string Name;
		public string Version;
		public string License;
		public string Copyright;
		public string Contact;
		public string Customizations;
		public override string ToString() {
			return Name + " (v" + Version + ")";
		}
	}
	
	/// <summary>
	/// Create a new mapper based on the XML file type and version.
	/// </summary>
	public static Mapper Create( string xml, Software sw ) {
		const int size = 2000;
		char[] buffer = new char[size];
		TextReader tr = new StreamReader( xml );
		tr.ReadBlock( buffer, 0, size );
		tr.Close();
		
		// PLGS
		string str = new string(buffer);
		if( str.Contains("GeneratedBy") )
			return new Plgs(sw);
		
		// mzIdentML 1.1
		int i = str.IndexOf( "MzIdentML" );
		string version;
		if( i != -1 ) {
			str = str.Remove(0,i);
			i = str.IndexOf( "version" );
			if( i == -1 )
				throw new ApplicationException( "mzIdentML version not provided" );
			str = str.Remove(0,i);
			version = str.Split(new char[]{'"'})[1];
			switch( version ) {
				case "1.1.0":
					return new mzId1_1(sw);
			}
			throw new ApplicationException( "mzIdentML version '" + version + "' not supported" );
		}
		
		// mzIdentML 1.0
		str = new string(buffer);
		i = str.IndexOf( "mzIdentML" );
		if( i == -1 )
			throw new ApplicationException( "Identification file format not supported" );
		str = str.Remove(0,i);
		i = str.IndexOf( "version" );
		if( i == -1 )
			throw new ApplicationException( "mzIdentML version not provided" );
		str = str.Remove(0,i);
		version = str.Split(new char[]{'"'})[1];
		switch( version ) {
			case "1.0.0":
				return new mzId1_0(sw);
		}
		throw new ApplicationException( "mzIdentML version '" + version + "' not supported" );
	}

	/// <summary>
	/// Default constructor
	/// </summary>
	public Mapper( Software sw ) {
		m_Software = sw;
		Proteins = new List<Protein>();
		Peptides = new List<Peptide>();
		m_Stats = new StatsStruct();
		m_Format = new System.Globalization.NumberFormatInfo();
		m_Format.NumberDecimalSeparator = ".";
		m_Run = 0;
		UsingThresholds = false;
		m_InputFiles = new List<string>();
	}
	
	/// <summary>
	/// Parser type.
	/// </summary>
	/// <returns>
	/// The name.
	/// </returns>
	public abstract string ParserName { get; }
	
	/// <summary>
	/// Loads data from a peptide identification file
	/// </summary>
	/// <param name="merge">
	/// A <see cref="System.Boolean"/> indicating wether merge the new file with the existing data
	/// </param>
	public void Load( string path, bool merge ) {
		if( !merge || m_Run == 0 ) {
			m_InputFiles.Clear();
			Proteins.Clear();
			Peptides.Clear();
			m_gid = 1;
			m_SortedProteins = new SortedList<string, Protein>();
			m_Run = 1;
		} else
			m_Run++;
		Load( path );
		m_InputFiles.Add( Path.GetFileName(path) );
	}
	
	/// <summary>
	/// Override to support different protein identification input file formats
	/// </summary>
	protected abstract void Load( string path );
	
	
	/// <summary>
	/// Saves results to a CSV file. Override to support other output file formats
	/// </summary>
	public virtual void Save( string fpath ) {
		SaveCSV( Path.ChangeExtension(fpath,".csv"), ":" );
		SaveHtml( Path.ChangeExtension(fpath,".html"), Path.GetFileNameWithoutExtension(fpath) );
	}
	
	/// <summary>
	/// Saves results to a CSV file.
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
	
	#region HTML
	
	/// <summary>
	/// Saves results to a HTML file.
	/// </summary>
	public void SaveHtml( string fpath, string name ) {
		TextWriter w = new StreamWriter(fpath);
		BeginHtml( w, name );
		EndHtml( w );
		w.Close();
	}
	
	private void BeginHtml( TextWriter w, string name ) {
		string title = "PAnalyzer: Protein identification report for " + name;
		Tag tr = new Tag( "tr", true );
		
		w.WriteLine( "<html>\n<head>" );
		w.WriteLine( "<title>" + title + "</title>"  );
		EmbedCss( w );
		w.WriteLine( "<body>" );
		w.WriteLine( "<a name=\"top\"/><h2>" + title + "</h2><hr/>" );
		w.WriteLine( "<table>\n<caption><a name=\"config\"/>Analysis Configuration</caption>" );
		w.WriteLine( tr.Render("<th>Software</th><td>" + m_Software + "</td>") );
		w.WriteLine( tr + "\n<th>Analysis type</th>" );
		if( m_InputFiles.Count == 1 ) {
			w.Write( "<td>Single run analysis</td>" + tr );
			w.WriteLine( tr.Render("<th>Input file</th>\n<td>" + m_InputFiles[0] + "</td>") );
		}
		else {
			w.WriteLine( "<td>Multirun analysis</td>" + tr );
			w.WriteLine( tr.Render("<th>Number of runs</th><td>"+m_InputFiles.Count+"</td>") );
			w.WriteLine( tr.Render("<th>Runs threshold</th><td>"+m_RunsTh+"</td>") );
			w.WriteLine( tr + "<th>Input files</th>\n<td>" );
			foreach( string f in m_InputFiles )
				w.WriteLine( f+"<br/>" );
			w.WriteLine( "</td>" + tr );
		}
		w.WriteLine( tr.Render( "<th>Input file type</th><td>"+ParserName+"</td>") );
		w.WriteLine( tr.Render("<th>Peptide threshold</th><td>" + m_Th + "</td>") );
		w.WriteLine( "</table>" );
	}
	
	private void EmbedCss( TextWriter w ) {
		w.WriteLine( "<style>" );
		w.WriteLine( "body { padding: 0px; margin: 20px; }" );
		w.WriteLine( "caption { font-size: 140%; color: darkgreen; text-align: left; }" );
		w.WriteLine( "th, td { padding-left: 2px; padding-right: 2px; }" );
		w.WriteLine( "th { text-align: left; }" );
		w.WriteLine( "tr.odd { background-color: #dddddd; }" );
		w.WriteLine( "tr.even { background-color: #eeeeee; }" );
		w.WriteLine( "table { border: 2px black solid; border-collapse: collapse; }" );
		w.WriteLine( "</style>" );
	}
	
	private void EndHtml( TextWriter w ) {
		w.WriteLine( "</body>\n</html>" );
	}
	
	#endregion
	
	/// <summary>
	/// Parses the confidence enum to a string.
	/// </summary>
	public string ParseConfidence( Protein.EvidenceType e ) {
		switch( e ) {
			case Protein.EvidenceType.Conclusive:
				return "conclusive";
			case Protein.EvidenceType.Group:
				return "ambiguous group";
			case Protein.EvidenceType.Indistinguishable:
				return "indistinguishable";
			case Protein.EvidenceType.NonConclusive:
				return "non conclusive";
		}
		return e.ToString();
	}
		
	/// <summary>
	/// Process the loaded data
	/// </summary>
	/// <param name="th">
	/// A <see cref="Peptide.ConfidenceType"/> indicating a threshold for peptide filtering
	/// </param>
	/// <param name="mode">
	/// A <see cref="int"/> indicating in how runs a peptide must be found for beeing considered as valid
	/// </param>
	public void Do( Peptide.ConfidenceType th, int runs ) {
		m_Th = th;
		m_RunsTh = runs;
		m_Run = 0;
		FilterPeptides();
		ClasifyPeptides();
		ClasifyProteins();
		DoStats();
	}
	
	/// <summary>
	/// Removes peptides with low score, duplicated (same sequence) or not voted (multirun)
	/// </summary>
	private void FilterPeptides() {
		List<Peptide> peptides = new List<Peptide>();
		int id = 1;
		
		// Remove previous relations
		foreach( Protein p in Proteins )
			p.Peptides.Clear();
		
		// Filters duplicated (same sequence) peptides
		SortedList<string,Peptide> SortedPeptides = new SortedList<string, Peptide>();
		foreach( Peptide f in Peptides ) {
			// Low score peptide
			if( (int)f.Confidence < (int)m_Th || f.Proteins.Count == 0 )
				continue;
			// Duplicated peptide, new protein?
			if( SortedPeptides.ContainsKey(f.Sequence) ) {
				Peptide fo = SortedPeptides[f.Sequence];
				if( (int)f.Confidence > (int)fo.Confidence )
					fo.Confidence = f.Confidence;
				if( !fo.Runs.Contains(f.Runs[0]) )
					fo.Runs.Add(f.Runs[0]);
				foreach( PTM ptm in f.PTMs )
					fo.AddPTM( ptm );
				bool dp = false;	// duplicated protein?, needed for PLGS
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
		if( m_RunsTh > 1 ) {
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
	
	protected void Notify( string message ) {
		if( OnNotify != null )
			OnNotify( message );
		else
			Console.WriteLine( message );
	}
	
	/// <summary>
	/// Returns counts
	/// </summary>
	public StatsStruct Stats {
		get { return m_Stats; }
	}
	
	/// <summary>
	/// Delegate used for sending messages from the lib to the app instead of using stdout
	/// </summary>
	public delegate void NotifyDelegate( string message );
	
	/// <summary>
	/// Event used for sending messages from the lib to the app instead of using stdout
	/// </summary>
	public event NotifyDelegate OnNotify;
	
	/// <summary>
	/// Protein list
	/// </summary>
	public List<Protein> Proteins;
	
	/// <summary>
	/// Peptide list
	/// </summary>
	public List<Peptide> Peptides;
	
	/// <summary>
	/// Indicates wether peptide scores and thresholds are used.
	/// </summary>
	public bool UsingThresholds;
		
	private int m_gid;
	private StatsStruct m_Stats;
	protected System.Globalization.NumberFormatInfo m_Format;
	protected SortedList<string,Protein> m_SortedProteins;
	protected int m_RunsTh, m_Run;
	protected List<string> m_InputFiles;
	protected Peptide.ConfidenceType m_Th;
	protected Software m_Software;
}

} // namespace EhuBio.Proteomics.Inference