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

namespace EhuBio.Proteomics.Inference {

/// <summary>
/// Infers proteins from peptide identifications
/// </summary>
public class mzId1_1 : Mapper {
	/// <summary>
	/// Constructor
	/// </summary>
	public mzId1_1(Mapper.Software sw) : base(sw) {
		//m_SWCustomizations = "No customizations";
	}
	
	protected override void Load (string path) {
		throw new NotImplementedException ();
	}
}

} // namespace EhuBio.Proteomics.Inference