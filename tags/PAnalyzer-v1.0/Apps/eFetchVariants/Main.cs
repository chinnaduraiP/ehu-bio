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
// Copyright (c) 2012 Gorka Prieto
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
using EhuBio.Database.Ehu;
using EhuBio.Database.Ncbi.eFetch.Snp;
using EhuBio.Database.Ncbi.eFetch.Sequences;

namespace eFetchVariants {

class MainClass {
	public static void Main (string[] args) {
		if( args.Length != 1 ) {
			Console.WriteLine( "variants file required" );
			return;
		}
		
		StreamReader rd = new StreamReader(new GZipStream(new FileStream(args[0],FileMode.Open), CompressionMode.Decompress));
		string line;		
		char[] sep1 = new char[]{','};
		char[] sep2 = new char[]{':'};
		string[] fields, fields2;
		String mut, prot;
		Variant v;
		Fasta f;
		List<string> ids = new List<string>();
		SortedList<string,Fasta> fasta = new SortedList<string, Fasta>();
		eFetchSnpService ssrv = new eFetchSnpService();		
		EhuBio.Database.Ncbi.eFetch.Snp.MessageEFetchRequest sreq = new EhuBio.Database.Ncbi.eFetch.Snp.MessageEFetchRequest();
		EhuBio.Database.Ncbi.eFetch.Snp.MessageEFetchResult sres;
		eFetchSequenceService psrv = new eFetchSequenceService();
		EhuBio.Database.Ncbi.eFetch.Sequences.MessageEFetchRequest preq = new EhuBio.Database.Ncbi.eFetch.Sequences.MessageEFetchRequest();
		EhuBio.Database.Ncbi.eFetch.Sequences.MessageEFetchResult pres;
		//int count = 1;
		while( (line=rd.ReadLine()) != null ) {
			fields = line.Split(sep1);
			if( !fields[2].Contains("/") || fields[3].Length == 0 || fields[3] != fields[4] )
				continue;
			v = new Variant();	
			if( ids.Contains(fields[0]) )
				continue;
			ids.Add(fields[0]);
			sreq.id = fields[0];
			sres = ssrv.run_eFetch( sreq );
			if( sres == null || sres.ExchangeSet.Rs == null || sres.ExchangeSet.Rs.Length == 0 )
				continue;
			Console.WriteLine( fields[0] + "..." );			
			foreach( string str in sres.ExchangeSet.Rs[0].hgvs ) {
				if( !str.StartsWith("NP_") )
					continue;
				Console.Write( str + " " );
				v = new Variant();
				v.id = str;
				fields2 = str.Split(sep2);
				mut = fields2[1]; prot = fields2[0];
				try {					
					v.orig = AminoAcid.Get(mut.Substring(2,3)).Letter;
					v.mut = AminoAcid.Get(mut.Substring(mut.Length-3,3)).Letter;
					v.pos = ulong.Parse(mut.Substring(5,mut.Length-8))-1;
				} catch {
					Console.WriteLine( "(filtered)" );
					continue;
				}
				if( fasta.ContainsKey(prot) ) {
					Console.WriteLine( "(cached)" );
					f = fasta[prot];
				} else {
					preq.db = "protein";
					preq.id = prot;
					pres = psrv.run_eFetch( preq );
					f = new Fasta(Fasta.Type.Protein,prot+"|"+pres.GBSet[0].GBSeq_definition,pres.GBSet[0].GBSeq_sequence);
					fasta.Add( prot, f );
					Console.WriteLine( "(downloaded)" );
				}
				f.mVariants.Add( v );				
			}
			/*if( --count == 0 )
				break;*/
		}
		
		foreach( Fasta fas in fasta.Values )
			fas.Dump( true );
	}
}

}	// namespace eFetchVariants