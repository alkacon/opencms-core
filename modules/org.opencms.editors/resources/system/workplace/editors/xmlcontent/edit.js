/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors/resources/system/workplace/editors/xmlcontent/edit.js,v $
 * Date   : $Date: 2005/09/29 12:48:27 $
 * Version: $Revision: 1.3.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
// Script for xml content editor 
//------------------------------------------------------//

// stores the opened window object
var treewin = null;

// function action on button click
function buttonAction(para) {	
	var _form = document.EDITOR;
	_form.target = "_self";
	submit(_form);

	switch (para) {
	case 1: 
		// exit editor without saving
		_form.action.value = actionExit;
		_form.target = "_top";		
		_form.submit();
		break;
	case 2:
		// save and exit editor
		_form.action.value = actionSaveExit;
		_form.submit();
		break;
	case 3:
		// save content
		_form.action.value = actionSave;
		_form.submit();
		break;
	case 4:
		// change element (change locale)
		_form.action.value = actionChangeElement;
		_form.submit();
		break;
	case 5:
		// add optional element
		_form.action.value = actionAddElement;
		_form.submit();
		break;
	case 6:
		// remove optional element
		_form.action.value = actionRemoveElement;
		_form.submit();
		break;
	case 7:
		// preview	
		_form.action.value = actionPreview;
		_form.target = "PREVIEW";
		openWindow = window.open("about:blank", "PREVIEW", "width=950,height=700,left=10,top=10,resizable=yes,scrollbars=yes,location=yes,menubar=yes,toolbar=yes,dependent=yes");
		_form.submit();
		break;	
	case 8:
		// check elements before performing customized action
		_form.action.value = actionCheck;
		_form.submit();
		break;
	case 9:
		// save and perform customized action
		_form.action.value = actionSaveAction;
		_form.target = "_top";
		_form.submit();
		break;
	default:
		alert("No action defined for this button!");
		break;
	}
}

function submit(form) {	
	try {
		// submit html editing areas if present
		submitHtml(form);
	} catch (e) {}
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

// add an optional element to the currently edited content
function addElement(elemName, insertAfter) {
	try {
		if (browser.isIE) {
			top.edit.buttonbar.lastPosY = document.body.scrollTop;	
		} else {
			top.edit.buttonbar.lastPosY = window.pageYOffset;
		}	
	} catch (e) {
		// ignore
	}
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = insertAfter;
	buttonAction(5);
}

// remove an optional element from currently edited content
function removeElement(elemName, index) {
	try {
		if (browser.isIE) {
			top.edit.buttonbar.lastPosY = document.body.scrollTop;	
		} else {
			top.edit.buttonbar.lastPosY = window.pageYOffset;
		}
	} catch (e) {
		// ignore
	}	
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = index;
	buttonAction(6);
}

// checks and adjusts the language selector in case an error is found in the edited content
function checkElementLanguage(newValue) {
	try {
		var langBox = parent.buttonbar.document.forms["buttons"].elements["elementlanguage"];
		if (langBox.value != newValue) {
			langBox.value = newValue;
		}
	} catch (e) {
		// ignore
	}
}

// submits the checked form for customized action button and considers delayed string insertion
function submitSaveAction() {
	if (! initialized) {
		setTimeout('submitSaveAction()', 20);
		return;
	}
	if (stringsPresent == true) {
		if (stringsInserted == true) {
			buttonAction(9);
		} else {
			setTimeout('submitSaveAction()', 20);
		}
	} else {
		buttonAction(9);
	}
}

// checks if the preview button is shown in the form for download or image galleries
function checkPreview(fieldId) {
	try {
		var theUri = document.getElementById(fieldId).value;
		theUri = theUri.replace(/ /, "");
		if ((theUri != "") && (theUri.charAt(0) == "/" || theUri.indexOf("http://") == 0)) {
			document.getElementById("preview" + fieldId).className = "show";
		} else {
			document.getElementById("preview" + fieldId).className = "hide";
		}
	} catch (e) {
		document.getElementById("preview" + fieldId).className = "hide";
	}
}

// scrolls the input form to the position where last element was added or removed
function scrollForm() {
	var posY = 0;
	try {
		posY = top.edit.buttonbar.lastPosY;
	} catch (e) {}
	window.scrollTo(0, posY);
}

// closes the popup window, this method is called by the onunload event
function closeTreeWin() {
	if (treewin != null) {
		// close the file selector window
		window.treewin.close();
		treewin = null;
		treeForm = null;
		treeField = null;
		treeDoc = null;
	}
}
