/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

//------------------------------------------------------//
// Script for editcontrol
//------------------------------------------------------//

// Indicates if the content of the editor window is already set
var contentSetted = false;

// loads the file content into the editor
function setContent() {
	// setting text can not be done now here for the text editor.
	// MS IE 5 has problems with setting text when the editor control is not loaded.
	// Workaround: focus() the text editor here and set the text
	// using the onFocus event of the editor.

	if (document.forms.EDITOR.edit1) {
		document.forms.EDITOR.edit1.focus();
	}
}

// load the file content into the editor. this is called by the onFocus event of the edit control
function setContentDelayed() {
	if(! contentSetted) {
		document.EDITOR.edit1.Text = decodeURIComponent(content);
		document.EDITOR.edit1.ClearModify(2);
		contentSetted = true;
	}
}


function saveContent()
{
	document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.Text);
}

// Function action on button click
function buttonAction(para)
{
	var _form = document.EDITOR;
	switch(para) {
	case 1:
	{
		saveContent();
		_form.action.value = "exit";
		_form.target = "_top";
		_form.submit();
		break;
	}
	case 2:
	{
		saveContent();
		_form.action.value = "saveexit";
		_form.target = "_top";
		_form.submit();
		break;
	}
	case 3:
	{
		saveContent();
		_form.action.value = "save";
		_form.submit();
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
		document.all.edit1.PrintText();
		break;
	}
	default:
	{
		alert("No action defined for this button!");
		break;
	}
	}
	document.EDITOR.edit1.focus();
}

// Opens popup window
function opensmallwin(url, name, w, h) {
	encodedurl = encodeURI(url);
	smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(smallwindow != null) {
		if (smallwindow.opener == null) {
			smallwindow.opener = self;
		}
	}
	return smallwindow;
}