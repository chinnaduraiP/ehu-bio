// $Id$
// 
// Fasta.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      Fasta.cs
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
using System.Collections.Generic;

namespace EhuBio.Database.Ehu {

public class Variant {
	public string id;
	public ulong pos;
	public char orig;
	public char mut;
	
	public override bool Equals( object obj ) {
		Variant v = (Variant)obj;
		//return v.id == id && v.pos == pos && v.orig == orig && v.mut == mut;
		return v.pos == pos && v.orig == orig && v.mut == mut;
	}
}

public class Fasta {
	public enum Type { Protein, Nucleotide };
	
	public Fasta( Type type, string header, string sequence ) {
		mType = type;
		char[] spaces = { ' ', '\t', '\r', '\n' };
		mHeader = header.Trim( spaces );
		mSequence = sequence.Trim( spaces );
		mVariants = new List<Variant>();
		//Validate();
	}
	
	public void Dump() {
		Dump ( false );
	}
	
	public void Dump( bool variants ) {
		Console.WriteLine( '>' + mHeader );
		int lines = mSequence.Length / 80;
		int i = 0;
		for( ; i < lines; i++ )
			Console.WriteLine( mSequence.Substring(i*80, 80) );
		Console.WriteLine( mSequence.Substring(i*80) );
		if( variants )
			if( mVariants.Count == 0 )
				Console.WriteLine( "No variants" );
			else
				foreach( Variant v in mVariants )
					Console.WriteLine( v.id + ": " + v.orig + "/" + v.mut + " (" + v.pos + ")" );
	}
	
	public void Validate() {
		throw new NotImplementedException();
		/*if( mType == Fasta.Type.Protein ) {
		} else {
		}*/
	}
	
	public string mHeader;
	public string mSequence;
	public Type mType;
	public List<Variant> mVariants;
}

}		// namespace EhuBio.Database.Ehu