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
using EhuBio.Proteomics.Hupo.mzIdentML1_0;
using EhuBio.Proteomics.Hupo.mzIdentML;

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Infers proteins from peptide identifications
/// </summary>
public class mzId1_0 : Mapper {
	/// <summary>
	/// Constructor
	/// </summary>
	public mzId1_0(Mapper.Software sw) : base(sw) {
	}
	
	/// <summary>
	/// Gets the name of the parser.
	/// </summary>
	/// <value>
	/// The name of the parser.
	/// </value>
	public override string ParserName {
		get {
			return "PSI-PI mzIdentML (v1.0.0)";
		}
	}
			
	/// <summary>
	/// Loads a mzIdentML file
	/// </summary>
	override protected void Load( string mzid ) {
		m_mzid = new mzidFile1_0();
		m_mzid.Load( mzid );
		
		// Proteins
		SortedList<string,string> SortedAccession = new SortedList<string, string>();
		foreach( PSIPIanalysissearchDBSequenceType element in m_mzid.ListProteins ) {
			if( SortedAccession.ContainsKey(element.id) )
				continue;
			string acc = element.accession;
			SortedAccession.Add( element.id, acc );
			if( m_SortedProteins.ContainsKey(acc) )
				continue;
			FuGECommonOntologycvParamType cv;
			cv = FuGECommonOntologycvParamType.Find( "MS:1001352", element.cvParam );
			string entry = cv == null ? "" : cv.value;
			cv = FuGECommonOntologycvParamType.Find( "MS:1001088", element.cvParam );
			string desc = cv == null ? "" : cv.value;
			string seq = element.seq;//.ToUpper();
			Protein p = new Protein(m_pid++, entry, acc, desc, seq);
			p.DBRef = element.id;
			Proteins.Add( p );
			m_SortedProteins.Add( acc, p );
		}
		
		// Peptides
		SortedList<string,Peptide> SortedPeptides = new SortedList<string, Peptide>();
		int id = 1;
		foreach( PSIPIpolypeptidePeptideType element in m_mzid.ListPeptides ) {
			string seq = element.peptideSequence;//.ToUpper();
			Peptide f = new Peptide(id++, seq);
			f.Confidence = Peptide.ConfidenceType.PassThreshold; // It will be filtered later if neccessary
			SortedPeptides.Add( element.id, f );
			f.Runs.Add( m_Run );
			if( element.Modification != null )
				foreach( PSIPIpolypeptideModificationType mod in element.Modification ) {
					PTM ptm = new PTM();
					ptm.Pos = mod.locationSpecified ? mod.location : -1;
					if( mod.residues != null )
						foreach( string residue in mod.residues )
							ptm.Residues += residue;
					foreach( FuGECommonOntologycvParamType param in mod.cvParam )
						if( param.cvRef.Equals("UNIMOD") )
							ptm.Name = param.name;
					f.AddPTM( ptm );
				}
			Peptides.Add( f );
		}
		
		// Relations
		if( m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList.Length != 1 )
			throw new ApplicationException( "Multiple spectrum identification lists not supported" );
		foreach( PSIPIanalysissearchSpectrumIdentificationResultType idres in
			m_mzid.Data.DataCollection.AnalysisData.SpectrumIdentificationList[0].SpectrumIdentificationResult )
			foreach( PSIPIanalysissearchSpectrumIdentificationItemType item in idres.SpectrumIdentificationItem ) {
				if( !item.passThreshold )
					continue;
				Peptide f = SortedPeptides[item.Peptide_ref];
				if( item.PeptideEvidence == null )
					continue;
				f.Confidence = Peptide.ConfidenceType.PassThreshold;
				foreach( PSIPIanalysisprocessPeptideEvidenceType relation in item.PeptideEvidence ) {
					Protein p = m_SortedProteins[SortedAccession[relation.DBSequence_Ref]];
					if( f.Proteins.Contains(p) )
						continue;
					f.Names.Add( relation.DBSequence_Ref, relation.id );
					p.Peptides.Add( f );
					f.Proteins.Add( p );
				}
			}
	}

	/// <summary>
	/// Save results to a mzIdentML file
	/// </summary>
	public void SaveMzid(
		string mzid,
		string org_id, string org_name,
		string owner_name, string owner_email ) {
		// Previous file is required for including MS data
		if( m_mzid == null )
			return;

		// Header
		//m_mzid.AddOntology( "PSI-MS", "Proteomics Standards Initiative Mass Spectrometry Vocabularies", "2.25.0",
        //	"http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo" );
        foreach( PSIPIanalysissearchAnalysisSoftwareType sw in m_mzid.ListSW )
        	if( sw.id == "PAnalyzer" ) {
        		m_mzid.ListSW.Remove(sw);
        		break;
        	}
        m_mzid.AddAnalysisSoftware(
            m_Software.Name, "UPV/EHU Protein Inference", m_Software.Version, "http://code.google.com/p/ehu-bio/", "UPV_EHU",
            "MS:1001267", "software vendor", "PSI-MS", m_Software.Customizations );
        foreach( FuGECommonAuditOrganizationType org in m_mzid.ListOrganizations )
        	if( org.id == "UPV_EHU" ) {
        		m_mzid.ListOrganizations.Remove( org );
        		break;
        	}
        m_mzid.AddOrganization( "UPV_EHU", "University of the Basque Country",
            "Barrio Sarriena s/n, 48940 Leioa, Spain", "+34 94 601 200", "secretariageneral@ehu.es" );
        if( org_id != null ) {
        	m_mzid.SetProvider( org_id, "DOC_OWNER", "MS:1001271", "researcher", "PSI-MS" );
        	m_mzid.AddPerson( "DOC_OWNER", owner_name, owner_email, org_id );
        	m_mzid.AddOrganization( org_id, org_name );
        }
        
        // Analysis
        List<PSIPIanalysisprocessProteinAmbiguityGroupType> listGroup =
        	new List<PSIPIanalysisprocessProteinAmbiguityGroupType>();
        int hit = 1;
        foreach( Protein p in Proteins ) {
        	PSIPIanalysisprocessProteinAmbiguityGroupType grp = new PSIPIanalysisprocessProteinAmbiguityGroupType();
        	grp.id = "PAG_hit_" + (hit++);
        	int num = (p.Subset.Count == 0 ? 1 : p.Subset.Count);
        	grp.ProteinDetectionHypothesis = new PSIPIanalysisprocessProteinDetectionHypothesisType[num];
        	if( p.Subset.Count == 0 )
        		grp.ProteinDetectionHypothesis[0] = BuildHypothesis( p );
        	else {
        		int i = 0;
        		foreach( Protein p2 in p.Subset )
        			grp.ProteinDetectionHypothesis[i++] = BuildHypothesis( p2 );
        	}
        	grp.cvParam = new FuGECommonOntologycvParamType[1];
        	grp.cvParam[0] = new FuGECommonOntologycvParamType(
        		"ProteomeDiscoverer:ProteinConfidenceCategory",
        		"MS:1001673", "PSI-MS" );
        	grp.cvParam[0].value = ParseConfidence( p.Evidence );
        	listGroup.Add( grp );
        }
        PSIPIanalysisprocessProteinDetectionListType analysis = new PSIPIanalysisprocessProteinDetectionListType();
        analysis.id = "PAnalyzer_PDL";
       	analysis.ProteinAmbiguityGroup = listGroup.ToArray();
        m_mzid.Data.DataCollection.AnalysisData.ProteinDetectionList = analysis;
        
        // Save
        m_mzid.Save( mzid );
	}
	
	/// <summary>
	/// Builds a PDH for the current protein
	/// </summary>
	private PSIPIanalysisprocessProteinDetectionHypothesisType BuildHypothesis( Protein p ) {
		PSIPIanalysisprocessProteinDetectionHypothesisType h = new PSIPIanalysisprocessProteinDetectionHypothesisType();
		h.id = "PDH_" + p.Accession;
		h.DBSequence_ref = p.DBRef;
		if( p.Evidence == Protein.EvidenceType.NonConclusive || p.Evidence == Protein.EvidenceType.Filtered )
			h.passThreshold = false;
		else
			h.passThreshold = true;
		List<PeptideHypothesisType> listPeptides = new List<PeptideHypothesisType>();
		foreach( Peptide f in p.Peptides ) {
			PeptideHypothesisType peptide = new PeptideHypothesisType();
			peptide.PeptideEvidence_Ref = f.Names[p.DBRef];
			listPeptides.Add( peptide );
		}
		if( listPeptides.Count > 0 )
			h.PeptideHypothesis = listPeptides.ToArray();
		return h;
	}
		
	private int m_pid;
	private mzidFile1_0 m_mzid;
}

} // namespace EhuBio.Proteomics.Inference