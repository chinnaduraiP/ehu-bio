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
using EhuBio.Database.Ehu;

namespace wregex {

public class WregexManager {
	public WregexManager( string RegexFile, string FastaFile ) {
		string line;
		char[] spaces = { ' ', '\t', '\r', '\n' };
		TextReader rd = new StreamReader( RegexFile );
		do {
			line = rd.ReadToEnd();
			if( line == null )
				break;
			line = line.Trim( spaces );
			if( line.Length == 0 || line[0] == '#' )
				continue;
			m_regex = line;
			break;
		} while( true );
	}
	
	public void Dump() {
		Console.WriteLine( "regex: " + (m_regex == null ? "<empty>" : m_regex) );
	}
	
	protected string m_regex;
	protected Fasta[] m_seqs;
}

}	// namespace wregex