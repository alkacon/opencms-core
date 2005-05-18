/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

// Indicates if the content of the editor window is already set
var contentSetted = false;

// loads the file content into the editor
function initContent() {
	document.getElementById("edit1").innerText = decodeURIComponent(text);
}

function saveContent() {
	document.EDITOR.content.value = encodeURIComponent(document.getElementById("edit1").innerText);
}

function doEdit(para) {
	document.getElementById("edit1").focus();
	var _form = document.EDITOR;
	switch(para) {
	case 1:
		saveContent();
		_form.action.value = actionExit;
		_form.target = "_top";
		_form.submit();
		break;
	case 2:
		saveContent();
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		_form.submit();
		break;
	case 3:
		saveContent();
		_form.action.value = actionSave;
		_form.target = "_self";
		_form.submit();
		break;
	case 4:
		saveContent();
		_form.action.value = actionSaveAction;
		_form.target = "_top";
		_form.submit();
		break;
	case 5:
		undo();
		break;  
	case 6:
		redo();
		break;    
	case 7:
		cut();
		break;
	case 8:
		copy();
		break;
	case 9:
		paste();
		break;
	default:
		alert("Sorry, the requested function " + para + " is not implemented.");
		break;  
	}   
}

function undo() {
	document.execCommand("Undo");   
}

function redo() {
	document.execCommand("Redo");   
}

function cut() {
	document.execCommand("Cut");   
}

function copy() {
	document.execCommand("Copy");   
}

function paste() {
	document.getElementById("edit1").setActive();
	document.getElementById("edit1").selection = document.selection.createRange();
	document.getElementById("edit1").selection.execCommand("Paste");   
}

function checkTab() {
	// cache the selection
	document.getElementById("edit1").selection =document.selection.createRange(); 
	setTimeout("processTab()", 0);
}

function processTab() {
	// insert tab character in place of cached selection
	document.getElementById("edit1").selection.text = String.fromCharCode(9);
	// set the focus
	document.getElementById("edit1").focus();
}

// This is not used on the code editor, but must be there since it is called on onLoad() event
function initStyles() {
	// noop
}