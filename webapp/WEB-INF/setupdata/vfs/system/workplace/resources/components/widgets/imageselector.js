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
 * When using this script to open the image gallery dialog, be sure to
 * initialize the context prefix (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - imContentPrefix = "<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";
 * - imgGalleryPath = "<%= CmsGalleryImages.C_URI_GALLERY %>";
 */

var imgContextPrefix;
var imgGalleryPath;

// opens the image gallery popup window, dialog mode has to be "widget" (as defined in CmsGallery.MODE_WIDGET)
function openImageSelector(dialogMode, fieldId) {
	var paramString = "?dialogmode=" + dialogMode;
	paramString += "&fieldid=" + fieldId;
	var treewin = window.open(imgContextPrefix + imgGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=550,height=700');
}

// opens a preview popup window to display the currently selected image
function previewImage(fieldId) {
	var imgUri = document.getElementById(fieldId).value;
	imgUri = imgUri.replace(/ /, "");
	if ((imgUri != "") && (imgUri.charAt(0) == "/")) {
		var treewin = window.open(imgContextPrefix + imgUri, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=550,height=550');
	}
}

// checks if the preview button is shown in the form
function checkPreview(fieldId) {
	var imgUri = document.getElementById(fieldId).value;
	imgUri = imgUri.replace(/ /, "");
	if ((imgUri != "") && (imgUri.charAt(0) == "/")) {
		document.getElementById("preview" + fieldId).className = "show";
	} else {
		document.getElementById("preview" + fieldId).className = "hide";
	}
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