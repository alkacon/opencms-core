/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */ 
var userWin = null;
var userForm = null;
var userField = null;
var userDoc = null;

function openUserWin(url, formName, fieldName, curDoc, flags, group) {
	userForm = formName;
	userField = fieldName;
	userDoc = curDoc;

	var paramString = "?type=userwidget";
	if (flags != null) {
		paramString += "&flags=";
		paramString += flags;
	}
	if (group != null) {
		paramString += "&group=";
		paramString += group;
	}	
	var userWin = window.open(url + paramString, 'opencms', 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=450,height=450');
	if(userWin != null) {
		if (userWin.opener == null) {
			userWin.opener = self;
		}
	}
	userWin.focus();
}

function setUserFormValue(username) {
	var curForm;
	var curDoc;
	if (userDoc != null) {
		curDoc = userDoc;
	} else {
		alert('no doc');
	}
	if (userForm != null) {
		curForm = curDoc.forms[userForm];	
	} else {
		alert('no form');
	}
	if (curForm.elements[userField]) {
		curForm.elements[userField].value = username;	
	}
}
