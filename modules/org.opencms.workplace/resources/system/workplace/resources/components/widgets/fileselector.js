/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
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
 
/*
 * When using this script to open the VFS file selector dialog, be sure to
 * initialize the tree in the opener properly:
 * (wp is an org.opencms.workplace.CmsWorkplace class)
 * <%= CmsTree.initTree(wp.getCms(), wp.getEncoding(), wp.getSkinUri()) %>
 */
 
var treewin = null;
var treeForm = null;
var treeField = null;
var treeDoc = null;

function openTreeWin(formName, fieldName, curDoc, showSiteSelector, startSite) {
	var paramString = "?type=vfswidget&includefiles=true&showsiteselector=";
	
	if (showSiteSelector) {
		paramString += "true";	
	} else {
		paramString += "false";
	}
	
	if (startSite != null) {
		paramString += "&treesite=";
		paramString += startSite;
	}

	treewin = openWin(vr.contextPath + vr.workplacePath + "views/explorer/tree_fs.jsp" + paramString, "opencms", 300, 450);
	treeForm = formName;
	treeField = fieldName;
	treeDoc = curDoc;
}

function openWin(url, name, w, h) {
	var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(newwin != null) {
		if (newwin.opener == null) {
			newwin.opener = self;
		}
	}
	newwin.focus();
	return newwin;
}

function setFormValue(filename) {
	var curForm;
	var curDoc;
	if (treeDoc != null) {
		curDoc = treeDoc;
	} else {
		curDoc = win.files;
	}
	if (treeForm != null) {
		curForm = curDoc.forms[treeForm];	
	} else {
		curForm = curDoc.forms[0];
	}
	if (curForm.elements[treeField]) {
		curForm.elements[treeField].value = filename;	
	}
}