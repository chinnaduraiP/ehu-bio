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
using System.Collections.Generic;
using System.Threading;
using EhuBio.Database.Ehu;

namespace wregex {

class WregexConsole {
	public static int Main( string[] args ) {
		if( args.Length < 2 || args.Length > 3 ) {
			DisplayUsage();
			return 1;
		} 
		
		string RegexFile = args[0];
		string PssmFile;
		string FastaFile;
		if( args.Length == 3 ) {
			PssmFile = args[1];
			FastaFile = args[2];
		} else {
			PssmFile = "";
			FastaFile = args[1];
		}
		
		// Use '.' as decimal separator
        Thread.CurrentThread.CurrentCulture = new System.Globalization.CultureInfo( "en-US", false );
		
		WregexConsole app = new WregexConsole();
		app.LoadData( RegexFile, PssmFile, FastaFile );
		//app.Dump();
		app.Run();
		
		return 0;
	}
	
	public static void DisplayUsage() {
		Console.WriteLine( "Usage:" );
		Console.WriteLine( "\twregex <regex_file> [<pssm_file>] <fasta_file>" );
	}
	
	private void LoadData( string RegexFile, string PssmFile, string FastaFile ) {
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
		rd = new UnixCfg( FastaFile );
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
		
		mDataId = Path.GetFileNameWithoutExtension( FastaFile );
	}
	
	private void Dump() {
		Console.WriteLine( "regex: " + (mRegex == null ? "<empty>" : mRegex.ToString()) );
		foreach( Fasta seq in mSeqs )
			seq.Dump();
	}
	
	private void Run() {
		List<WregexResult> results = GetResults();
		WriteAln( results );
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