// $Id$
// 
// Main.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      Main.cs
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
using System.IO.Compression;
using System.Collections.Generic;
using System.Threading;
using EhuBio.Database.Ehu;
using EhuBio.Database.Ebi;

namespace wregex {

class WregexConsole {
	public static int Main( string[] args ) {
		if( args.Length % 2 != 0 ) {
			DisplayUsage( "odd number of arguments" );
			return 1;
		}
		
		string RegexFile="";
		string PssmFile="";
		string DatabaseFile="";
		string VariantsFile="";
		for( int i = 0; i < args.Length; i += 2 ) {
			if( args[i][0] != '-' || args[i].Length != 2 ) {
				DisplayUsage( "incorrect argument specifier" );
				return 1;
			}
			switch( args[i][1] ) {
				case 'd': DatabaseFile = args[i+1]; break;
				case 'r': RegexFile = args[i+1]; break;
				case 'p': PssmFile = args[i+1]; break;
				case 'v': VariantsFile = args[i+1]; break;
				default: DisplayUsage( "specifier '" + args[i] + "'not known" ); return 1;
			}
		}		
		if( DatabaseFile.Length == 0 || RegexFile.Length == 0 ) {
			DisplayUsage( "missing mandatory parameters" );
			return 1;
		}
		if( !DatabaseFile.Contains(".fasta") && !DatabaseFile.Contains(".xml.gz") ) {
			DisplayUsage( "database file format not recognized" );
			return 1;
		}
		if( !DatabaseFile.Contains(".xml.gz") && VariantsFile.Length != 0 ) {
			DisplayUsage( "variants are only supported with UniProt XML input file" );
			return 1;
		}
		
		// Use '.' as decimal separator
        Thread.CurrentThread.CurrentCulture = new System.Globalization.CultureInfo( "en-US", false );
		
		WregexConsole app = new WregexConsole();
		app.LoadData( RegexFile, PssmFile, DatabaseFile, VariantsFile );
		//app.Dump();
		app.Run();
		
		return 0;
	}
	
	public static void DisplayUsage( string err ) {
		Console.WriteLine( "ERROR: " + err );
		Console.WriteLine( "\nUsage:" );
		Console.WriteLine( "\twregex -r <regex_file> [-p <pssm_file>] -d <file.fasta|file.xml.gz> [-v <variants.txt.gz>]" );
	}
	
	private void LoadData( string RegexFile, string PssmFile, string DatabaseFile, string VariantsFile ) {
		string line;
				
		// regex
		UnixCfg rd = new UnixCfg( RegexFile );
		line = rd.ReadUnixLine();
		if( line == null )
			throw new ApplicationException( "Empty regex" );
		rd.Close();
		if( PssmFile.Length > 0 )
			mRegex = new WregexManager( line, new PSSM(PssmFile) );
		else
			mRegex = new WregexManager( line );
		
		// Fasta
		mSeqs = new List<Fasta>();
		if( DatabaseFile.Contains(".fasta") )
			LoadFasta( DatabaseFile );
		else {
			SortedList<string,List<Variant>> list = null;
			if( VariantsFile.Length != 0 )
				list = LoadVariants( VariantsFile );
			LoadXml( DatabaseFile, list );
		}
		
		mDataId = Path.GetFileNameWithoutExtension( DatabaseFile );
	}
	
	private SortedList<string,List<Variant>> LoadVariants( string path ) {		
		StreamReader rd = new StreamReader(new GZipStream(new FileStream(path,FileMode.Open), CompressionMode.Decompress));
		SortedList<string,List<Variant>> list = new SortedList<string,List<Variant>>();
		string line;
		char[] sep1 = new char[]{','};
		char[] sep2 = new char[]{'/'};
		string[] fields, fields2;
		Variant v;
		while( (line=rd.ReadLine()) != null ) {
			fields = line.Split(sep1);
			if( !fields[2].Contains("/") || fields[5].Length == 0 || fields[3].Length == 0 || fields[3] != fields[4] )
				continue;
			v = new Variant();
			v.id = fields[5];
			v.pos = ulong.Parse(fields[3]);
			fields2 = fields[2].Split(sep2);
			v.orig = fields2[0][0];
			v.mut = fields2[1][0];
			if( v.mut == '*' )
				continue;
			if( list.ContainsKey(v.id) )
				if( list[v.id].Contains(v) )
					continue;
				else
					list[v.id].Add(v);
			else {
				list[v.id] = new List<Variant>();
				list[v.id].Add( v );
			}
		}
		return list;
	}
	
	private void LoadFasta( string path ) {
		string line;
		UnixCfg rd = new UnixCfg( path );
		string seq = "";
		string header = rd.ReadUnixLine();
		if( header == null || header[0] != '>' )
			throw new ApplicationException( "FASTA header not found" );
		do {
			line = rd.ReadUnixLine();
			if( line == null || line[0] == '>' ) {
				if( seq.Length == 0 )
					throw new ApplicationException( "FASTA sequence not found" );
				mSeqs.Add( new Fasta( Fasta.Type.Protein, header.Substring(1), seq) );
				header = line;
				seq = "";
			} else
				seq += line;
		} while( line != null );
		rd.Close();
	}
	
	private void LoadXml( string path, SortedList<string,List<Variant>> list ) {
		UniprotXml xml = new UniprotXml( path );
		EhuBio.Database.Ebi.Xml.entry e;
		Fasta f = null;
		bool skip = false;
		while( (e=xml.ReadEntry()) != null ) {
			if( e.sequence == null || e.sequence.Value == null || e.sequence.Value.Length == 0 )
				continue;				
			if( list != null ) {
				skip = true;
				foreach( EhuBio.Database.Ebi.Xml.featureType feature in e.feature )
					if( feature.type == EhuBio.Database.Ebi.Xml.featureTypeType.sequencevariant && feature.id != null )
						if( list.ContainsKey(feature.id) ) {
							if( skip == true ) {
								f = new Fasta( Fasta.Type.Protein, e.accession[0], e.sequence.Value );
								skip = false;
							}
							f.mVariants.AddRange( list[feature.id] );
						}
			} else
				f = new Fasta( Fasta.Type.Protein, e.accession[0], e.sequence.Value );
			if( skip )
				continue;
			f.Dump( true );			
			mSeqs.Add( f );
		}
		xml.Close();
	}
	
	private void Dump() {
		Console.WriteLine( "regex: " + (mRegex == null ? "<empty>" : mRegex.ToString()) );
		foreach( Fasta seq in mSeqs )
			seq.Dump();
	}
	
	private void Run() {
		List<WregexResult> results = GetResults();
		if( results.Count == 0 ) {
			Console.WriteLine( "No matches found, please consider reviewing the regex" );
			return;
		}
		WriteAln( results );
		WriteCsv( results );
		ShowResults( results );
	}
	
	private List<WregexResult> GetResults() {
		List<WregexResult> results = new List<WregexResult>();
		List<WregexResult> tmp_results;
		char[] sep = new char[]{ ' ' };
		
		Console.WriteLine( "Searching with '" + mRegex + "' ...\n" );
		foreach( Fasta seq in mSeqs ) {
			tmp_results = mRegex.Search( seq.mSequence, seq.mHeader.Split(sep)[0] );
			if( tmp_results == null )
				continue;
			seq.Dump();
			foreach( WregexResult result in tmp_results ) {
				results.Add( result );
				Console.WriteLine( "* Match!! -> " + result.Match +
					" (" + result.Index + ".." + (result.Index+result.Length-1) + ") -> " +
					result.ToString() );
			}
			Console.WriteLine();
		}
		
		results.Sort(delegate(WregexResult r1, WregexResult r2) {
			if( r1.Score > r2.Score )
				return -1;
			if( r1.Score < r2.Score )
				return 1;
			return 0;
		});
		
		return results;
	}
	
	private void WriteAln( List<WregexResult> results ) {
		int count = results[0].Groups.Count+1;
		int[] gsizes = new int[count];
		int i, j, gsize;
		
		// Calculate lengths for further alignment
		for( i = 0; i < count; i++ )
			gsizes[i] = 0;
		foreach( WregexResult result in results ) {
			gsize = result.Id.Length;
			if( gsize > gsizes[0] )
				gsizes[0] = gsize;
			for( i = 1; i < count; i++ ) {
				gsize = result.Groups[i-1].Length;
				if( gsizes[i] < gsize )
					gsizes[i] = gsize;
			}
		}
		
		TextWriter wr = new StreamWriter( mDataId + ".aln", false );
		wr.WriteLine( "CLUSTAL 2.1 multiple sequence alignment (by WREGEX)\n\n" );
		foreach( WregexResult result in results ) {
			wr.Write( result.Id );
			for( j = result.Id.Length; j < gsizes[0]+4; j++ )
				wr.Write( ' ' );
			for( i = 1; i < count; i++ ) {
				wr.Write( result.Groups[i-1] );
				for( j = result.Groups[i-1].Length; j < gsizes[i]; j++ )
					wr.Write( '-' );
			}
			wr.WriteLine();
		}
		wr.WriteLine();
		wr.Close();
	}
	
	private void WriteCsv( List<WregexResult> results ) {
		TextWriter wr = new StreamWriter( mDataId + ".csv", false );
		wr.WriteLine( "ID,Entry,Pos,Combinations,Sequence,Alignment,Score" );
		foreach( WregexResult result in results )
			wr.WriteLine( result.Id + "," + result.Entry + "," + result.Position + "," + result.Combinations
				+ "," + result.Match + "," + result.Alignment + "," + result.Score );
		wr.Close();
	}
	
	private void ShowResults( List<WregexResult> results ) {
		foreach( WregexResult res in results ) {
			Console.WriteLine( res.ToString() );
		}
	}
	
	private WregexManager mRegex;
	private List<Fasta> mSeqs;
	private string mDataId;
}

}	// namespace wregex