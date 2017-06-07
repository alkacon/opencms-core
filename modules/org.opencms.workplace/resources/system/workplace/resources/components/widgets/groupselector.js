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
var groupWin = null;
var groupForm = null;
var groupField = null;
var groupDoc = null;

function openGroupWin(url, formName, fieldName, curDoc, flags, user, oufqn) {
	groupForm = formName;
	groupField = fieldName;
	groupDoc = curDoc;

	var paramString = "?type=groupwidget";
	if (flags != null) {
		paramString += "&flags=";
		paramString += flags;
	}
	if (user != null) {
		paramString += "&user=";
		paramString += user;
	}
	if (oufqn != null) {
	    paramString += "&oufqn=";
		paramString += oufqn;
	}	
	var groupWin = window.open(url + paramString, 'opencms', 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=450,height=450');
	if(groupWin != null) {
		if (groupWin.opener == null) {
			groupWin.opener = self;
		}
	}
	groupWin.focus();
}

function setGroupFormValue(groupname) {
	var curForm;
	var curDoc;
	if (groupDoc != null) {
		curDoc = groupDoc;
	} else {
		alert('no doc');
	}
	if (groupForm != null) {
		curForm = curDoc.forms[groupForm];	
	} else {
		alert('no form');
	}
	if (curForm.elements[groupField]) {
		curForm.elements[groupField].value = groupname;	
	}
}
