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
var ouWin = null;
var ouForm = null;
var ouField = null;
var ouDoc = null;

function openOrgUnitWin(url, formName, fieldName, curDoc, role) {
	ouForm = formName;
	ouField = fieldName;
	ouDoc = curDoc;

	var paramString = "?type=orgunitwidget";
	if (role != null) {
		paramString += "&role=";
		paramString += role;
	}
	
	var ouWin = window.open(url + paramString, 'opencms', 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=450,height=450');
	if(ouWin != null) {
		if (ouWin.opener == null) {
			ouWin.opener = self;
		}
	}
	ouWin.focus();
}

function setOrgUnitFormValue(oufqn) {
	var curForm;
	var curDoc;
	if (ouDoc != null) {
		curDoc = ouDoc;
	} else {
		alert('no doc');
	}
	if (ouForm != null) {
		curForm = curDoc.forms[ouForm];	
	} else {
		alert('no form');
	}
	if (curForm.elements[ouField]) {
		curForm.elements[ouField].value = oufqn;	
	}
}
