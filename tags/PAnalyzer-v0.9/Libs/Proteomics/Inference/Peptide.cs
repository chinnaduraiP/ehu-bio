// $Id$
// 
// Peptide.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      Peptide.cs
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

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Class for managing peptide information and relations
/// </summary>
public class Peptide {
	/// <summary>
	/// Peptide-protein relations
	/// </summary>
	public enum RelationType { Unique, Meaningful, Meaningless };
	
	/// <summary>
	/// Peptide confidence according to its score and a given threshold
	/// </summary>
	public enum ConfidenceType { Red, Yellow, Green, PassThreshold };
	
	/// <summary>
	/// Constructor
	/// </summary>
	/// <param name="ID">
	/// A <see cref="System.Int32"/> to uniquely identify the peptide
	/// </param>
	/// <param name="Seq">
	/// A <see cref="System.String"/> with the peptide sequence
	/// </param>
	public Peptide( int ID, string Seq ) {
		this.ID = ID;
		this.Sequence = Seq;
		Confidence = Peptide.ConfidenceType.Green;
		Relation = Peptide.RelationType.Meaningless;
		Proteins = new List<Protein>();
		Runs = new List<int>();
		Names = new SortedList<string, string>();
	}
	
	/// <summary>
	/// Builds an string with a '*' code for indicating the peptide type
	/// </summary>
	public override string ToString() {
		switch( Relation ) {
			case RelationType.Unique:
				return ID.ToString();
			case RelationType.Meaningful:
				return ID.ToString()+'*';
			case RelationType.Meaningless:
				return ID.ToString()+"**";
		}
		return "?";
	}
	
	/// <summary>
	/// Sets the peptide score updating its confidence type
	/// </summary>
	public double Score {
		set {
			if( value >= GreenTh )
				Confidence = Peptide.ConfidenceType.Green;
			else if( value >= YellowTh )
				Confidence = Peptide.ConfidenceType.Yellow;
			else
				Confidence = Peptide.ConfidenceType.Red;
		}
	}
	
	/// <summary>
	/// Peptide ID
	/// </summary>	
	public int ID;
	
	/// <summary>
	/// Peptide sequence
	/// </summary>
	public string Sequence;
	
	/// <summary>
	/// List of runs in which the peptide is present
	/// </summary>
	public List<int> Runs;
	
	/// <summary>
	/// Peptide confidence according to its score and threshold
	/// </summary>
	public ConfidenceType Confidence;
	
	/// <summary>
	/// Peptide type
	/// </summary>
	public RelationType Relation;
	
	/// <summary>
	/// List of proteins in which this peptide is present
	/// </summary>
	public List<Protein> Proteins;
	
	/// <summary>
	/// Peptide identification names used in mzIdentML v1.0
	/// </summary>
	public SortedList<string,string> Names;
	
	/// <summary>
	/// Red-Yellow threshold
	/// </summary>
	public static double YellowTh = 0.0;
	
	/// <summary>
	/// Yellow-Green threshold
	/// </summary>
	public static double GreenTh = 0.0;
}

} // namespace EhuBio.Proteomics.Inference