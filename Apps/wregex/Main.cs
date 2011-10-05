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

namespace wregex {

class WregexConsole {
	public static int Main( string[] args ) {
		if( args.Length != 2 ) {
			DisplayUsage();
			return 1;
		}
		
		string RegexFile = args[0];
		string FastaFile = args[1];
		
		WregexManager wrx = new WregexManager( RegexFile, FastaFile );
		wrx.Dump();
		
		return 0;
	}
	
	public static void DisplayUsage() {
		Console.WriteLine( "Usage:" );
		Console.WriteLine( "\twregex <regex_file> <fasta_file>" );
	}
}

}	// namespace wregex