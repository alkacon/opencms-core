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
 
/*
 * When using this script to open the html gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - htmlGalleryPath = "<%= CmsGallery.C_PATH_GALLERIES + CmsGallery.C_OPEN_URI_SUFFIX + "?" + CmsGallery.PARAM_GALLERY_TYPENAME + "=htmlgallery" %>";
 */

var htmlGalleryPath;

// opens the download gallery popup window, dialog mode has to be "widget" (as defined in CmsGallery.MODE_WIDGET)
function openHtmlGallery(dialogMode, fieldId) {
	var paramString = "&dialogmode=" + dialogMode;
	paramString += "&fieldid=" + fieldId;
	treewin = window.open(contextPath + htmlGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=650,height=700');
}

// pastes the content from hidden field to div
function checkHtmlContent(fieldId) {
	var content = document.getElementById(fieldId).value;
	var prevDiv = document.getElementById("html." + fieldId);
	if (content == null || content == "") {
		content = "&nbsp;";	
	}
	prevDiv.innerHTML = content;
}

// resets the content from hidden field and div
function resetHtmlGallery(fieldId) {
	document.getElementById(fieldId).value = "";
	checkHtmlContent(fieldId);
}

