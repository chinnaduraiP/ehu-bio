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
	
	/// <summary>
	/// Gets the name of the parser.
	/// </summary>
	/// <value>
	/// The name of the parser.
	/// </value>
	public override string ParserName {
		get {
			return "PSI-PI mzIdentML (v1.1.0)";
		}
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
	
	/// <summary>
	/// Also saves as mzid.
	/// </summary>
	public override void Save( string fpath ) {
		base.Save( fpath );
		SaveMzid( Path.ChangeExtension(fpath,".mzid") );
	}
	
	/// <summary>
	/// Save results to a mzIdentML file
	/// </summary>
	public void SaveMzid( string fpath ) {
		if( m_mzid == null || m_InputFiles.Count > 1 )
			return;
		
		#region Organization
		OrganizationType org = new OrganizationType();
		org.id = "UPV/EHU";
		org.name = "University of the Basque Country";
		foreach( OrganizationType o in m_mzid.ListOrganizations )
			if( o.id == org.id ) {
				m_mzid.ListOrganizations.Remove( o );
				break;
			}
		m_mzid.ListOrganizations.Add( org );
		#endregion
		
		#region Software author
		PersonType person = new PersonType();
		person.id = "PAnalyzer_Author";
		person.firstName = "Gorka";
		person.lastName = "Prieto";
		CVParamType email = new CVParamType();
		email.accession = "MS:1000589";
		email.name = "contact email";
		email.cvRef = "PSI-MS";
		email.value = "gorka.prieto@ehu.es";
		person.Item = email;
		AffiliationType aff = new AffiliationType();
		aff.organization_ref = org.id;
		person.Affiliation = new AffiliationType[]{aff};
		foreach( PersonType p in m_mzid.ListPeople )
			if( p.id == person.id ) {
				m_mzid.ListPeople.Remove( p );
				break;
			}
		m_mzid.ListPeople.Add( person );
		#endregion

		#region Analysis software
		AnalysisSoftwareType sw = new AnalysisSoftwareType();
		sw.id = m_Software.Name;
		sw.name = m_Software.ToString();
		sw.uri = m_Software.Url;
		sw.version = m_Software.Version;
		UserParamType swname = new UserParamType();
		swname.name = sw.name;
		sw.SoftwareName = new ParamType();
		sw.SoftwareName.Item = swname;
		ContactRoleType contact = new ContactRoleType();
		contact.contact_ref = person.id;
		RoleType role = new RoleType();
		CVParamType contacttype = new CVParamType();
		contacttype.accession = "MS:1001271";
		contacttype.cvRef = "PSI-MS";
		contacttype.name = "researcher";
		role.cvParam = contacttype;
		sw.ContactRole = new ContactRoleType();
		sw.ContactRole.contact_ref = person.id;
		sw.ContactRole.Role = role;
		sw.Customizations = m_Software.Customizations;
		foreach( AnalysisSoftwareType s in m_mzid.ListSW )
			if( s.id == m_Software.Name ) {
				m_mzid.ListSW.Remove( sw );
				break;
			}
		m_mzid.ListSW.Add( sw );
		#endregion
		
		#region Protein detection protocol
		//ProteinDetectionProtocolType pdp = new ProteinDetectionProtocolType();
		m_mzid.Data.AnalysisProtocolCollection.ProteinDetectionProtocol.analysisSoftware_ref = sw.id;
		m_mzid.Data.AnalysisProtocolCollection.ProteinDetectionProtocol.id = "PDP_PAnalyzer_1";
		/*pdp.AnalysisParams = new ParamListType();
		UserParamType up = new UserParamType();
		up.name = "Peptide Threshold";
		up.value = m_Th.ToString();
		pdp.AnalysisParams.Item = up;
		pdp.Threshold = new ParamListType();
		up = new UserParamType();
		up.name = "Conclusive evidence";
		pdp.Threshold.Item = up;
		m_mzid.Data.AnalysisProtocolCollection.ProteinDetectionProtocol = pdp;*/
		#endregion
		
		#region Protein detection list
		SortedList<string,ProteinDetectionHypothesisType> list = new SortedList<string, ProteinDetectionHypothesisType>();
		List<ProteinAmbiguityGroupType> groups = new List<ProteinAmbiguityGroupType>();
		foreach( ProteinAmbiguityGroupType grp in m_mzid.Data.DataCollection.AnalysisData.ProteinDetectionList.ProteinAmbiguityGroup )
			foreach( ProteinDetectionHypothesisType pdh in grp.ProteinDetectionHypothesis )
				list.Add( pdh.dBSequence_ref, pdh );
		foreach( Protein p in Proteins ) {
			ProteinAmbiguityGroupType g = new ProteinAmbiguityGroupType();
			CVParamType ev = new CVParamType();
			ev.accession = "MS:1001600";
			ev.cvRef = "PSI-MS";
			ev.name = "Protein Inference Confidence Category";
			switch( p.Evidence ) {
				case Protein.EvidenceType.Conclusive:
					ev.value = "conclusive"; break;
				case Protein.EvidenceType.Indistinguishable:
					ev.value = "indistinguishable"; break;
				case Protein.EvidenceType.Group:
					ev.value = "ambiguous group"; break;
				case Protein.EvidenceType.NonConclusive:
					ev.value = "non conclusive"; break;
				default:
					continue;
			}
			g.Item = ev;
			if( p.Subset.Count == 0 )
				g.ProteinDetectionHypothesis = new ProteinDetectionHypothesisType[]{list[p.DBRef]};
			else {
				List<ProteinDetectionHypothesisType> listpdh = new List<ProteinDetectionHypothesisType>();
				foreach( Protein p2 in p.Subset )
					listpdh.Add( list[p2.DBRef] );
				g.ProteinDetectionHypothesis = listpdh.ToArray();
			}
			groups.Add( g );
		}
		m_mzid.Data.DataCollection.AnalysisData.ProteinDetectionList.id = "PAnalyzer_PDL";
		m_mzid.Data.DataCollection.AnalysisData.ProteinDetectionList.ProteinAmbiguityGroup = groups.ToArray();
		#endregion
		
		m_mzid.Save( fpath );
		Notify( "Saved to " + fpath );
	}
	
	private int m_pid = 0;
	private mzidFile1_1 m_mzid;
}

} // namespace EhuBio.Proteomics.Inference