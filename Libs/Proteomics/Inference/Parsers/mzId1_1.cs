// $Id: Mapper.cs 22 2012-03-06 19:50:52Z gorka.prieto@gmail.com $
// 
// Mapper.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      Mapper.cs
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
using System.Xml;
using EhuBio.Proteomics.Hupo.mzIdentML;
using EhuBio.Proteomics.Hupo.mzIdentML1_1;

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Infers proteins from peptide identifications
/// </summary>
public class mzId1_1 : Mapper {
	/// <summary>
	/// Constructor
	/// </summary>
	public mzId1_1(Mapper.Software sw) : base(sw) {
	}
	
	protected override void Load( string mzid ) {
		m_mzid = new mzidFile1_1();
		m_mzid.Load( mzid );
		
		// Proteins
		SortedList<string,string> SortedAccession = new SortedList<string, string>();
		foreach( DBSequenceType prot in m_mzid.ListProteins ) {
			if( SortedAccession.ContainsKey(prot.id) )	// Avoids duplicated entries in the same file
				continue;
			SortedAccession.Add( prot.id, prot.accession );
			if( m_SortedProteins.ContainsKey(prot.accession) ) // Avoids duplicated entries between different files
				continue;
			CVParamType cv = mzidFile1_1.FindCV("MS:1001352", prot.Item);
			string entry = cv == null ? "" : cv.value;
			cv = mzidFile1_1.FindCV("MS:1001088", prot.Item);
			string desc = cv == null ? "" : cv.value;
			Protein p = new Protein( m_pid++, entry, prot.accession, desc, prot.Seq );
			p.DBRef = prot.id;
			Proteins.Add( p );
			m_SortedProteins.Add( p.Accession, p );
		}
		
		// Peptides
		SortedList<string,Peptide> SortedPeptides = new SortedList<string, Peptide>();
		int id = 1;
		foreach( PeptideType pep in m_mzid.ListPeptides ) {
			Peptide p = new Peptide( id++, pep.PeptideSequence );
			p.Confidence = Peptide.ConfidenceType.PassThreshold; // It will be filtered later if neccessary
			SortedPeptides.Add( pep.id, p );
			p.Runs.Add( m_Run );
			if( pep.Modification != null )
				foreach( ModificationType mod in pep.Modification ) {
					PTM ptm = new PTM();
					ptm.Pos = mod.locationSpecified ? mod.location : -1;
					if( mod.residues != null )
						foreach( string residue in mod.residues )
							ptm.Residues += residue;
					foreach( CVParamType param in mod.cvParam )
						if( param.cvRef.Equals("UNIMOD") )
							ptm.Name = param.name;
					p.AddPTM( ptm );
				}
			p.DBRef = pep.id;
			Peptides.Add( p );
		}
		
		// Relations
		if( m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList.Length != 1 )
			throw new ApplicationException( "Multiple spectrum identification lists not supported" );
		SortedList<string,PeptideEvidenceType> SortedEvidences = new SortedList<string, PeptideEvidenceType>();
		foreach( PeptideEvidenceType evidence in m_mzid.Data.SequenceCollection.PeptideEvidence )
			SortedEvidences.Add( evidence.id, evidence );
		foreach( SpectrumIdentificationResultType idres in
			m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList[0].SpectrumIdentificationResult )
			foreach( SpectrumIdentificationItemType item in idres.SpectrumIdentificationItem ) {
				if( !item.passThreshold )
					continue;
				foreach( PeptideEvidenceRefType evref in item.PeptideEvidenceRef ) {
					PeptideEvidenceType evidence = SortedEvidences[evref.peptideEvidence_ref];
					Peptide pep = SortedPeptides[evidence.peptide_ref];
					Protein prot = m_SortedProteins[SortedAccession[evidence.dBSequence_ref]];
					if( pep.Proteins.Contains(prot) )
						continue;
					prot.Peptides.Add( pep );
					pep.Proteins.Add( prot );
				}
			}
	}
	
	private int m_pid = 0;
	private mzidFile1_1 m_mzid;
}

} // namespace EhuBio.Proteomics.Inference