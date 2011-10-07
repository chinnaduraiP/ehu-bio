// $Id$
// 
// WregexManager.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      WregexManager.cs
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

public class WregexManager {
	public WregexManager( string RegexFile, string FastaFile ) {
		string line;
		
		// regex
		TextReader rd = new StreamReader( RegexFile );
		line = ReadUnixLine( rd );
		if( line == null )
			throw new ApplicationException( "Empty regex" );
		rd.Close();
		mRegexStr = line;
		
		// Fasta
		List<Fasta> list = new List<Fasta>();
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
				list.Add( new Fasta( Fasta.Type.Protein, header.Substring(1), seq) );
				header = line;
				seq = "";
			} else
				seq += line;
		} while( line != null );
		mSeqs = list.ToArray();
	}
	
	public void Dump() {
		Console.WriteLine( "regex: " + (mRegexStr == null ? "<empty>" : mRegexStr) );
		foreach( Fasta seq in mSeqs )
			seq.Dump();
	}
	
	private string ReadUnixLine( TextReader rd ) {
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
	
	protected string mRegexStr;
	protected Fasta[] mSeqs;
}

}	// namespace wregex