/*
* File   : $Source: $
* Date   : $Date: $
* Version: $Revision: $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
 
//------------------------------------------------------//
// Script for simple text editor
//------------------------------------------------------//

// Definition of constants
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;

// Indicates if the text of the editor window is already set
var textSetted = false;

// loads the file content into the editor
function setText()
{
    // setting text can not be done now here for the text editor.
    // MS IE 5 has problems with setting text when the editor control is not loaded. 
    // Workaround: focus() the text editor here and set the text
    // using the onFocus event of the editor.
    if (document.forms.EDITOR.edit1) {
    	document.forms.EDITOR.edit1.focus();
   	}
}

// load the file content into the editor. this is called by the onFocus event of the edit control
function setTextDelayed()
{
	if(! textSetted) {
    	document.EDITOR.edit1.value = decodeURIComponent(text);
		textSetted = true;
	}
}


// function action on button click for Netscape Navigator
function doNsEdit(para)
{
	// We have to do a blur on the textarea here. Otherwise Netscape may have problems with reading the value
    document.EDITOR.edit1.blur();
    document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
    
    switch(para)
    {
    case 1:
    {
        document.EDITOR.action.value = "exit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 2:
    {
        document.EDITOR.action.value = "saveexit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 3:
    {
        document.EDITOR.action.value = "save";
        document.EDITOR.submit();
        break;
    }
    }
}