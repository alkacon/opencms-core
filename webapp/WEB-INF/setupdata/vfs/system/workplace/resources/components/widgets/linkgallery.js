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
 * When using this script to open the link gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - linkGalleryPath = "<%= CmsGallery.C_PATH_GALLERIES + CmsGallery.C_OPEN_URI_SUFFIX + "?" + CmsGallery.PARAM_GALLERY_TYPENAME + "=linkgallery" %>";
 */

var linkGalleryPath;

// opens the link gallery popup window, dialog mode has to be "widget" (as defined in CmsGallery.MODE_WIDGET)
function openLinkSelector(dialogMode, fieldId) {
	var paramString = "&dialogmode=" + dialogMode;
	paramString += "&fieldid=" + fieldId;
	treewin = window.open(contextPath + linkGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=650,height=700');
}

// opens a preview popup window to display the currently selected link
function previewLink(fieldId) {
	var linkUri = document.getElementById(fieldId).value;
	linkUri = linkUri.replace(/ /, "");
	if ((linkUri != "") && (linkUri.charAt(0) == "/" || linkUri.indexOf("http://") == 0)) {
		if (linkUri.charAt(0) == "/") {
			linkUri = contextPath + linkUri;
		}
		treewin = window.open(linkUri, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=750,height=700');
	}
}