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
using EhuBio.Database.Ehu;

namespace wregex {

class WregexConsole {
	public static int Main( string[] args ) {
		if( args.Length != 2 ) {
			DisplayUsage();
			return 1;
		} 
		
		string RegexFile = args[0];
		string FastaFile = args[1];
		
		WregexConsole app = new WregexConsole();
		app.LoadData( RegexFile, FastaFile );
		//app.Dump();
		app.Run();
		
		return 0;
	}
	
	public static void DisplayUsage() {
		Console.WriteLine( "Usage:" );
		Console.WriteLine( "\twregex <regex_file> <fasta_file>" );
	}
	
	private static string ReadUnixLine( TextReader rd ) {
		string line;
		char[] spaces = { ' ', '\t', '\r', '\n' };
		
		while( rd.Peek() >= 0 ) {
			line = rd.ReadLine();
			line = line.Trim( spaces );
			if( line.Length == 0 || line[0] == '#' )
				continue;
			return line;
		};
		
		return null;
	}
	
	private void LoadData( string RegexFile, string FastaFile ) {
		string line;
		
		// regex
		TextReader rd = new StreamReader( RegexFile );
		line = ReadUnixLine( rd );
		if( line == null )
			throw new ApplicationException( "Empty regex" );
		rd.Close();
		mRegex = new WregexManager( line );
		
		// Fasta
		mSeqs = new List<Fasta>();
		rd = new StreamReader( FastaFile );
		string seq = "";
		string header = ReadUnixLine( rd );
		if( header == null || header[0] != '>' )
			throw new ApplicationException( "FASTA header not found" );
		do {
			line = ReadUnixLine( rd );
			if( line == null || line[0] == '>' ) {
				if( seq.Length == 0 )
					throw new ApplicationException( "FASTA sequence not found" );
				mSeqs.Add( new Fasta( Fasta.Type.Protein, header.Substring(1), seq) );
				header = line;
				seq = "";
			} else
				seq += line;
		} while( line != null );
		
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
				Console.Write( "* Match!! -> " + result.Match +
					" (" + result.Index + ".." + (result.Index+result.Length-1) + ") -> " +
					result.Groups[0] );
				for( int i = 1; i < result.Groups.Count; i++ )
					Console.Write( "-" + result.Groups[i] );
				Console.WriteLine();
			}
			Console.WriteLine();
		}
		
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
	
	private WregexManager mRegex;
	private List<Fasta> mSeqs;
	private string mDataId;
}

}	// namespace wregex