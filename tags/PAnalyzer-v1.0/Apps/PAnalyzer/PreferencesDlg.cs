// $Id$
// 
// PreferencesDlg.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      PreferencesDlg.cs
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
using EhuBio.Proteomics.Inference;

public partial class PreferencesDlg : Gtk.Dialog {
	public PreferencesDlg() {
		this.Build();
	}
	
	public bool ThSensitive {
		set { ThCombo.Sensitive = value; }
	}
	
	public bool MultiRunSensitive {
		set { MultirunCombo.Sensitive = value; }
	}
	
	public Peptide.ConfidenceType Threshold {
		get { return (Peptide.ConfidenceType)ThCombo.Active; }
		set { ThCombo.Active = (int)value; }
	}
	
	public int RunTh {
		set {
			MultirunCombo.Active = value-1;
		}
		get {
			return MultirunCombo.Active+1;
		}
	}
	
	public int Runs {
		set {
			for( int i = 0; i < 10; i++ )
				MultirunCombo.RemoveText(0);
			//MultirunCombo = Gtk.ComboBox.NewText();
			for( int i = 1; i <= value; i++ )
				MultirunCombo.AppendText( i.ToString() );
			MultirunCombo.Active = 0;
		}
	}
}