/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace/resources/system/workplace/resources/components/widgets/principalselector.js,v $
 * Date   : $Date: 2007/02/09 10:29:15 $
 * Version: $Revision: 1.1.2.1 $
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
var principalWin = null;
var principalForm = null;
var principalField = null;
var principalDoc = null;
var typeField = null;  // this must be explicitly set if needed

function openPrincipalWin(url, formName, fieldName, curDoc, flags) {
	principalForm = formName;
	principalField = fieldName;
	principalDoc = curDoc;

	var paramString = "?type=principalwidget&flags=";
	paramString += flags;
	
	var principalWin = window.open(url + paramString, 'opencms', 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=260,width=650,height=450');
	if(principalWin != null) {
		if (principalWin.opener == null) {
			principalWin.opener = self;
		}
	}
	principalWin.focus();
}

function setPrincipalFormValue(principalType, principalName) {
	var curForm;
	var curDoc;
	if (principalDoc != null) {
		curDoc = principalDoc;
	} else {
		alert('no doc');
	}
	if (principalForm != null) {
		curForm = curDoc.forms[principalForm];	
	} else {
		alert('no form');
	}
    var principal = principalName;
	if (curForm.elements[typeField]) {
		curForm.elements[typeField].value = principalType;	
	} else {
	    if (principalType == 0) {
	    	principal = "GROUP." + principal;
	    } else {
	    	principal = "USER." + principal;
	    }
	}
	if (curForm.elements[principalField]) {
		curForm.elements[principalField].value = principal;	
	} else {
		alert('no field: ' + principalField);
	}
}
