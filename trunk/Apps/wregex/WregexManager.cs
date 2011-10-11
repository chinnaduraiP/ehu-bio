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
using System.Text.RegularExpressions;
using EhuBio.Database.Ehu;

namespace wregex {

public struct WregexResult {
	public string Match;
	public int Index;
	public int Length;
	public List<string> Groups;
	public double Score;	
}

public class WregexManager {
	public WregexManager( string RegexStr ) {
		mRegex = new Regex( RegexStr, RegexOptions.IgnoreCase | RegexOptions.ECMAScript | RegexOptions.Multiline | RegexOptions.Compiled );
	}
	
	public override string ToString() {
		return mRegex == null ? "" : mRegex.ToString();
	}
	
	public List<WregexResult> Search( string seq ) {
		MatchCollection Matches = mRegex.Matches(seq);
		if( Matches.Count == 0 )
			return null;
		
		List<WregexResult> results = new List<WregexResult>();
		WregexResult result;
		foreach( Match m in Matches ) {
			result = new WregexResult();
			result.Groups = new List<string>();
			result.Match = m.Value;
			result.Index = m.Index;
			result.Length = m.Length;
			for( int i = 1; i < m.Groups.Count; i++ )
				result.Groups.Add( m.Groups[i].Value );
			results.Add( result );
		}
		return results;
	}
	
	protected Regex mRegex;
}

}	// namespace wregex