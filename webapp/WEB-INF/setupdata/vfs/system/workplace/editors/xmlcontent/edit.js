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
// Script for xml content editor 
//------------------------------------------------------//

// function action on button click
function buttonAction(para) {
	var _form = document.EDITOR;

	switch (para) {
	case 1: 
		// exit editor without saving
		_form.action.value = actionExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 2:
		// save and exit editor
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 3:
		// save content
		_form.action.value = actionSave;
		submit(_form);
		_form.submit();
		break;
	case 4:
		// change element (change locale)
		_form.action.value = actionChangeElement;
		_form.target = "_self";
		submit(_form);
		_form.submit();
		break;
	case 5:
		// add optional element
		_form.action.value = actionAddElement;
		submit(_form);
		_form.submit();
		break;
	case 6:
		// remove optional element
		_form.action.value = actionRemoveElement;
		submit(_form);
		_form.submit();
		break;
	case 9:
		// save and perform customized action
		_form.action.value = actionSaveAction;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	default:
		alert("No action defined for this button!");
		break;
	}
}

function submit(form) {	
	try {
		// submit html areas if present
		submitHtmlArea(form);
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

function addElement(elemName, insertAfter) {
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = insertAfter;
	buttonAction(5);
}

function removeElement(elemName, index) {
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = index;
	buttonAction(6);
}