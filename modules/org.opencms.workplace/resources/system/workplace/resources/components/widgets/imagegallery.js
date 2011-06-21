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
 * When using this script to open the image gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - imageGalleryPath = "<%= A_CmsAjaxGallery.PATH_GALLERIES + A_CmsAjaxGallery.OPEN_URI_SUFFIX + "?" %>";
 */

var imageGalleryPath;
var imageGalleryInfo;

/* Returns the value of the specified scale parameter. */
/* Copy from vfsimage.js*/
function getScaleValue(scale, valueName) {
	if (scale == null) {
		return "";
	}
	var pos = scale.indexOf(valueName + ":");
	if (pos != -1) {
		// found value, return it
		if (pos > 0 && (valueName == "h" || valueName == "w")) {
			// special handling for "w" and "h", could also match "cw" and "ch"
			if (scale.charAt(pos - 1) == "c") {
				scale = scale.substring(pos + 1);
			}
		}
		var searchVal = new RegExp(valueName + ":\\d+,*", "");
		var result = scale.match(searchVal);
		if (result != null && result != "") {
			result = result.toString().substring(valueName.length + 1);
			if (result.indexOf(",") != -1) {
				result = result.substring(0, result.indexOf(","));
			}
			return result;
		}
	}	
	return "";
}

function setImageGalleryInfo(fieldId, idHash) {

	// parameter from the xml configuration and the input field
	var imageEl = window.document.getElementById(fieldId);
	// image path with scale parameteres as ?__scale=
	var imagePath = imageEl.value;
	
	var showFormats = eval('useFmts' + idHash);
	var useFormats = false;
	if (showFormats == true) {
		var formatNames = escape(eval('imgFmtNames' + idHash));
		var formatValues = eval('imgFmts' + idHash);
		if (formatNames.toString() != "null") {
			useFormats = showFormats;
		}
	}
	
	var scaleParam = extractScaleParam(imagePath);
	var imgWidth = "";
	var imgHeight = "";
	if (scaleParam != null) {
		imgWidth = getScaleValue(scaleParam, "w");
		imgHeight = getScaleValue(scaleParam, "h");
	}
		
	var editedResource = "";
	try {
		editedResource = document.forms["EDITOR"].elements["resource"].value;
	} catch (e) {};

	var startupFolder = eval('startupFolder' + idHash);
	var startupType = eval('startupType' + idHash);
		
	imageGalleryInfo = {
		
		"fieldid": 		fieldId,
		"hashid": 		idHash,
		"imagepath": 		imagePath,
		"useformats": 		useFormats,
		"showformats": 		showFormats,
		"scale":		    scaleParam,
		"imgwidth":		    imgWidth,
		"imgheight":		imgHeight,
		"editedresource": 	editedResource,
		"startupfolder":	startupFolder,
		"startuptype": 		startupType
	};


}

// opens the image gallery popup window, dialog mode has to be "widget" (as defined in A_CmsGallery.MODE_WIDGET)
// TO DO: extract galleryInfo to a method, see vfsimage.js
function openImageGallery(dialogMode, fieldId, idHash) {

	setImageGalleryInfo(fieldId, idHash);
	var paramString = "dialogmode=" + dialogMode;
	paramString += "&widgetmode=simple";
	paramString += "&params=" + JSON.stringify(imageGalleryInfo);
	
	treewin = window.open(contextPath + imageGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=650,height=700');


}

// opens a preview popup window to display the currently selected image
function previewImage(fieldId) {
	var imgUri = document.getElementById(fieldId).value;
	imgUri = imgUri.replace(/ /, "");
	if ((imgUri != "") && (imgUri.charAt(0) == "/")) {
		treewin = window.open(contextPath + imgUri, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=550,height=550');
	}
}

/* extracts the scale parameter from the imagepath if available */
function extractScaleParam(pathWithParam) {
	var path = "";
	var index = pathWithParam.indexOf("?__scale=");
	if (index == -1) {
		path = path;
	} else {
		path = pathWithParam.substring(index + 9);
	}
	return path;
}