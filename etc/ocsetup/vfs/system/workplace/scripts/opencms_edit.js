/*
* File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/opencms_edit.js,v $
* Date   : $Date: 2002/09/03 11:57:00 $
* Version: $Revision: 1.18 $
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
// Script for editcontrol
//------------------------------------------------------//

// Definition of constants
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;

var UNDO=4;
var REDO=5;

var SEARCH=6;
var REPLACE=7;
var GOTO=8;

var CUT=9;
var COPY=10;
var PASTE=11;

var IMPORT=12;
var EXPORT=13;
var EXPORTAS=14;

var PRINT=15;

// Indicates if the text of the editor window is already set
var textSetted = false;
var isLedit = false;

// loads the file content into the editor
function setText()
{
    // setting text can not be done now here for the text editor.
    // MS IE 5 has problems with setting text when the editor control is
    // not loaded. 
    // Workaround: focus() the text editor here and set the text
    // using the onFocus event of the editor.

    if(document.forms.EDITOR.edit1) document.forms.EDITOR.edit1.focus();
}

// load the file content into the editor. this is called by the onFocus event of the edit control
function setTextDelayed()
{
	var classid = "" + document.EDITOR.edit1.classid;
	isLedit = (classid.indexOf("EB3A74C0") >= 0);
	if(! textSetted) {
		if (isLedit) {
			document.EDITOR.edit1.Text = decodeURIComponent(text);
			document.EDITOR.edit1.ClearModify(2);
		} else {
        	document.EDITOR.edit1.value = decodeURIComponent(text);
		}
		textSetted = true;
	}
}


function doSubmit()
{
    if(isLedit) {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.Text);
    } else {
        // We have to do a blur on the textarea here. otherwise netscape may have problems with reading the value
        document.EDITOR.edit1.blur();
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
    }
}

// Function action on button click for Netscape Navigator
function doNsEdit(para)
{
    switch(para)
    {
    case 1:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "exit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 2:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "saveexit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 3:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "save";
        document.EDITOR.submit();
        break;
    }
    }
}

// Function action on button click for MS IE
function doEdit(para)
{
    switch(para)
    {
    case 1:
    {
        doSubmit();
        document.EDITOR.action.value = "exit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 2:
    {
        doSubmit();
        document.EDITOR.action.value = "saveexit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 3:
    {
        doSubmit();
        document.EDITOR.action.value = "save";
        document.EDITOR.submit();
        break;
    }
    case 4:
    {
        document.all.edit1.Undo();
        break;  
    }
    case 5:
    {
        document.all.edit1.Redo();
        break;  
    }
    case 6:
    {
        document.all.edit1.ShowFindDialog();
        break;
    }
    case 7:
    {
        document.all.edit1.ShowReplaceDialog();
        break;
    }
    case 8:
    {
        document.all.edit1.ShowGotoLineDialog();
        break;  
    }
    case 9:
    {
        document.all.edit1.CutToClipboard();
        break;
    }
    case 10:
    {
        document.all.edit1.CopyToClipboard();
        break;
    }
    case 11:
    {
        document.all.edit1.PasteFromClipboard();
        break;
    }
    case 12:
    {
        document.all.edit1.OpenFile();
        break;
    }
    case 13:
    {
        document.all.edit1.SaveFile();
        break;
    }
    case 14:
    {
        document.all.edit1.SaveFileAs();
        break;
    }
    case 15:
    {
        document.all.edit1.PrintText();
        break;
    }
    case 16:
    {
        // dummy tag for help
        break;
    }
    default:
    {
        alert("NYI");
        break;
    }
    }   
    document.EDITOR.edit1.focus();
}


// This is not used on the code editor, but must be there since it is called on onLoad() event
function initStyles() {
}