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
// Script for simple page editor
//------------------------------------------------------//

// dialog windows
var dialogElementWindow = null;
var dialogPropertyWindow = null;

// Indicates if the content of the editor window is already set
var contentSetted = false;

// loads the file content into the editor
function initContent() {
	// setting text can not be done now here for the text editor.
	// MS IE 5 has problems with setting text when the editor textarea is
	// not loaded.
	// Workaround: focus() the text editor here and set the text
	// using the onFocus event of the textarea.
	if (document.forms.EDITOR.edit1) document.forms.EDITOR.edit1.focus();
}

// load the file content into the editor. This is called by the onFocus event of the edit textarea
function initContentDelayed() {
	if(!contentSetted) {
		document.EDITOR.edit1.value = decodeURIComponent(__content);
		contentSetted = true;
	}
}

// saves the editors contents
function saveContent() {
	// we have to do a blur on the textarea here. Otherwise Netscape may have problems with reading the value
	document.EDITOR.edit1.blur();
	document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
}

// action on button click
function buttonAction(para) {
	var _form = document.EDITOR;
	_form.action.value = "";
	switch (para) {
	case 1:
		// reload the editor
		saveContent();
		_form.action.value = actionShow;
		_form.target = "_self";
		_form.submit();
		break;
	case 2:
		// preview selected
		saveContent();
		_form.action.value = actionPreview;
		_form.target = "PREVIEW";
		_form.submit();
		break;
	case 3:
		// change element
		saveContent();
		_form.action.value = actionChangeElement;
		_form.target = "_self";
		_form.submit();
		break;
	case 4:
		// open elements window
		saveContent();
		dialogElementWindow = window.open("about:blank","DIALOGELEMENT","width=320,height=250,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,dependent=yes");
		document.ELEMENTS.submit();
		dialogElementWindow.focus();
		break;
	case 5:
		// open properties window
		saveContent();
		dialogPropertyWindow = window.open("about:blank","DIALOGPROPERTY","width=600,height=280,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,dependent=yes");
		document.PROPERTIES.submit();
		dialogPropertyWindow.focus();
		break;
	case 6:
		// exit without saving
		saveContent();
		_form.action.value = actionExit;
		_form.target = "_top";
		_form.submit();
		break;
	case 7:
		// save and exit
		saveContent();
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		_form.submit();
		break;
	case 8:
		// save
		saveContent();
		_form.action.value = actionSave;
		_form.target = "_self";
		_form.submit();
		break;
	case 9:
		// save and perform customized action
		saveContent();
		_form.action.value = actionSaveAction;
		_form.target = "_top";
		_form.submit();
		break;
	}
}

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

// EVENT: all browsers have to check the keydown event
if (document.captureEvents) {
	//non IE
	if(Event.KEYDOWN) {
		//NS 4, NS 6+, Mozilla 0.9+
		document.captureEvents(Event.KEYDOWN);
	}
}
/* this next line tells the browser to detect a keydown
event over the whole document and when it detects it,
it should run the event handler function 'keyDownHandler' */
document.onkeydown = keyDownHandler;

function keyDownHandler(e) {
	// EVENT HANDLER: handle tab key in text edit mode
	if (!e) {
		// if the browser did not pass the event information to the
		// function, we will have to obtain it from the event register
		if (window.event) {
			//DOM
			e = window.event;
		} else {
			// total failure, we have no way of referencing the event
			return;
		}
	}

	if (typeof(e.which) == 'number') {
		//NS 4, NS 6+, Mozilla 0.9+, Opera
		key = e.which;
	} else if (typeof(e.keyCode) == 'number') {
		//IE, NS 6+, Mozilla 0.9+
		key = e.keyCode;
	} else if (typeof(e.charCode) == 'number') {
		//also NS 6+, Mozilla 0.9+
		key = e.charCode;
	} else {
		// total failure, we have no way of obtaining the key code
		return;
	}

	switch (key) {
		case 9:
		// prevent switching focus if tabulator key is pressed
		checkTab();
		break;
	}
}

function addText(input, insText) {
	input.focus();
	if (input.createTextRange) {
		document.selection.createRange().text += insText;
	} else if (input.setSelectionRange) {
		var len = input.selectionEnd;
		input.value = input.value.substr(0, len)
			+ insText + input.value.substr(len);
		input.setSelectionRange(len + insText.length, len + insText.length);
	} else {
		input.value += insText;
	}
}

function checkTab() {
	input = document.getElementById("edit1");
	if (input.createTextRange || input.setSelectionRange) {
		input = document.getElementById("edit1");
		if (input.createTextRange) {
			document.getElementById("edit1").selection = document.selection.createRange();
		}
		setTimeout("addText(input, String.fromCharCode(9))", 0);
	}
}