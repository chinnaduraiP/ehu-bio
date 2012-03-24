// $Id$
// 
// MyClass.cs
//  
// Author:
//      Gorka Prieto <gorka.prieto@gmail.com>
// 
// Description:
//      MyClass.cs
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

namespace EhuBio.UI.Html {

/// <summary>
/// Tag with state.
/// </summary>
public class Tag {
	/// <summary>
	/// Initializes a new instance of the <see cref="EhuBio.UI.Html.Tag"/> class.
	/// </summary>
	/// <param name='name'>
	/// Name.
	/// </param>
	public Tag( string name ) {
		mName = name;
		mOdd = true;
		mOpen = false;
		OddEvenEnabled = false;
	}
	
	/// <summary>
	/// Initializes a new instance of the <see cref="EhuBio.UI.Html.Tag"/> class.
	/// </summary>
	/// <param name='name'>
	/// Name.
	/// </param>
	/// <param name='state'>
	/// Wether to used odd/even state.
	/// </param>
	public Tag( string name, bool state ) : this( name ) {
		OddEvenEnabled = true;
	}
	
	/// <summary>
	/// Returns a <see cref="System.String"/> that represents the current state of <see cref="EhuBio.UI.Html.Tag"/>.
	/// </summary>
	/// <returns>
	/// A <see cref="System.String"/> that represents the current state of <see cref="EhuBio.UI.Html.Tag"/>.
	/// </returns>
	public override string ToString() {
		string res;
		if( !mOpen ) {
			if( !OddEvenEnabled )
				res = "<" + mName + ">";
			else if( mOdd )
				res = "<" + mName + " class=\"odd\">";
			else
				res = "<" + mName + " class=\"even\">";
		} else {
			res = "<" + mName + "/>";
			mOdd = !mOdd;
		}
		mOpen = !mOpen;
		return res;
	}
	
	/// <summary>
	/// Renders the specified value between open and close tags.
	/// </summary>
	/// <param name='val'>
	/// Value.
	/// </param>
	public string Render( string val ) {
		return ToString() + val + ToString();
	}
	
	/// <summary>
	/// Resets odd/even state.
	/// </summary>
	public void Reset() {
		mOdd = true;
	}
	
	/// <summary>
	/// Enables the odd and even classes.
	/// </summary>
	public bool OddEvenEnabled;
	
	protected string mName;
	private bool mOdd;
	private bool mOpen;
}

}