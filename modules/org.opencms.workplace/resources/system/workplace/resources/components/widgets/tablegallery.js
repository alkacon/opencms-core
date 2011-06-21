/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * When using this script to open the table gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - tableGalleryPath = "<%= A_CmsGallery.PATH_GALLERIES + A_CmsGallery.OPEN_URI_SUFFIX + "?" + A_CmsGallery.PARAM_GALLERY_TYPENAME + "=tablegallery" %>";
 * - tableGalleryPath for CmsAjaxTableGallery = "<%= A_CmsAjaxGallery.PATH_GALLERIES + A_CmsAjaxTableGallery.OPEN_URI_SUFFIX + "?" %>";
 */

var tableGalleryPath;
var tableGalleryInfo;

// opens the table gallery popup window, dialog mode has to be "widget" (as defined in A_CmsAjaxGallery.MODE_WIDGET)
function openTableGallery(dialogMode, fieldId, idHash) {
	//parameter from the xml configuration
	var startupFolder = eval('startupFolder' + idHash);
	var startupType = eval('startupType' + idHash);
	//edited resource has to be provided to use custom categories
	var editedResource = "";
	try {
		editedResource = document.forms["EDITOR"].elements["resource"].value;
	} catch (e) {};
	tableGalleryInfo = {
		"startupfolder": 	startupFolder,
		"startuptype": 		startupType,
		"editedresource": 	editedResource
	};

	var paramString = "dialogmode=" + dialogMode;
	paramString += "&fieldid=" + fieldId;
	paramString += "&params=" + JSON.stringify(tableGalleryInfo);
	treewin = window.open(contextPath + tableGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=650,height=700');
}

// pastes the content from hidden field to div
function checkTableContent(fieldId) {
	var content = document.getElementById(fieldId).value;
	var prevDiv = document.getElementById("table." + fieldId);
	if (content == null || content == "") {
		content = "&nbsp;";	
	}
	prevDiv.innerHTML = content;
}

// resets the content from hidden field and div
function resetTableGallery(fieldId) {
	document.getElementById(fieldId).value = "";
	checkTableContent(fieldId);
}

