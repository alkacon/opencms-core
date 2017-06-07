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
var roleWin = null;
var roleForm = null;
var roleField = null;
var roleDoc = null;

function openRoleWin(url, formName, fieldName, curDoc) {
	roleForm = formName;
	roleField = fieldName;
	roleDoc = curDoc;
	
	var roleWin = window.open(url, 'opencms', 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=450,height=450');
	if(roleWin != null) {
		if (roleWin.opener == null) {
			roleWin.opener = self;
		}
	}
	roleWin.focus();
}

function setRoleFormValue(rolefqn) {
	var curForm;
	var curDoc;
	if (roleDoc != null) {
		curDoc = roleDoc;
	} else {
		alert('no doc');
	}
	if (roleForm != null) {
		curForm = curDoc.forms[roleForm];	
	} else {
		alert('no form');
	}
	if (curForm.elements[roleField]) {
		curForm.elements[roleField].value = rolefqn;	
	}
}
