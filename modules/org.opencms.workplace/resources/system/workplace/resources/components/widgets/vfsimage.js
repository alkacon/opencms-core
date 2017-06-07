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
 
/*
 * When using this script to open the image gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly:
 *
 * - vfsImageGalleryPath = "<%= A_CmsAjaxGallery.PATH_GALLERIES + CmsAjaxImageGallery.OPEN_URI_SUFFIX%>";
 */

var vfsImageGalleryPath;

var vfsImageGalleryInfo;

/* Triggered when the image format selector changes, removes the crop parameters if present. */
function setImageFormat(valId, idHash) {
	var field = document.getElementById("fmtval." + valId);
	var selBox = document.getElementById("format." + valId);
	var allValues = eval(idHash);
	field.value = allValues[selBox.selectedIndex];	
	var scaleEl = document.getElementById("scale." + valId);
	if (scaleEl != null) {
		scale = scaleEl.value;
		if (field.value != null && field.value.length > 0) {
			// we have a format value, check scale parameters
			// for eventual crop info to remove
			scale = removeScaleValue(scale, "cx");
			scale = removeScaleValue(scale, "cy");
			scale = removeScaleValue(scale, "cw");
			scale = removeScaleValue(scale, "ch");
		}
		if (field != null) {
			var ratioField = document.getElementById("imgrat." + valId);
	  		formatValue = field.value;
	  		var pos = formatValue.indexOf("x");
	  		if (pos != -1) {
	  			imgWidth = formatValue.substring(0, pos);
	  			imgHeight = formatValue.substring(pos + 1);
	  			var widthInt = 0;
	  			var heightInt = 0;
	  			var rat = 1;
		  		try {
		  			rat = parseFloat(ratioField.value);
		  		} catch (e) {}
		  		if (imgWidth == "?") {
		  			heightInt = parseInt(imgHeight);
		  			widthInt = Math.round(heightInt * rat);
		  		} else if (imgHeight == "?") {
		  			widthInt = parseInt(imgWidth);
		  			heightInt = Math.round(widthInt / rat);
		  		} else {
		  			heightInt = parseInt(imgHeight);
		  			widthInt = parseInt(imgWidth);
		  		}
		  		scale = removeScaleValue(scale, "w");
		  		scale = removeScaleValue(scale, "h");
		  		if (scale != "" && scale.charAt(scale.length - 1) != ",") {
		  			scale += ",";
		  		}
		  		scale += "w:" + widthInt + ",h:" + heightInt;
	  		}
	  	}
		scaleEl.value = scale;
	}
}

/* Fills all available image information in a vfsImageGalleryInfo object. */
function setVfsImageGalleryInfo(valId, idHash) {
	// get form elements
	var imageEl = document.getElementById("img." +valId);
	var formatNameEl = document.getElementById("format." + valId);
	var formatValueEl = document.getElementById("fmtval." + valId);
	var descEl = document.getElementById("desc." + valId);
	var scaleEl = document.getElementById("scale." + valId);
	var editedResource = "";
	try {
		editedResource = document.forms["EDITOR"].elements["resource"].value;
	} catch (e) {};
	
	// initialize values for object
	var imagePath = imageEl.value;
	var formatName = "";
	var formatValue = "";
	var desc = "";
	var scale = "";
	var imgWidth = "";
	var imgHeight = "";
	
	var useFormats = eval('useFmts' + idHash);
	
	if (useFormats == true && formatNameEl != null) {
		formatName = formatNameEl.options[formatNameEl.selectedIndex].value;	
	}
	if (formatValueEl != null) {
		formatValue = formatValueEl.value;
		var pos = formatValue.indexOf("x");
		if (pos != -1) {
			imgWidth = formatValue.substring(0, pos);
			imgHeight = formatValue.substring(pos + 1);
		}
	}
	if (descEl != null) {
		// the description is currently not used in the gallery
		desc = descEl.value;	
	}

	if (scaleEl != null) {
		scale = scaleEl.value;
		if (formatValue != null && formatValue.length > 0) {
			// we have a format value, check scale parameters
			// for eventual width and height info and remove them if a format selector is present
			if (useFormats == true) {
				scale = removeScaleValue(scale, "w");
				scale = removeScaleValue(scale, "h");
			}
		} else {
			imgWidth = getScaleValue(scale, "w");
			imgHeight = getScaleValue(scale, "h");
		}
	}
	
	var startupFolder = eval('startupFolder' + idHash);
	var startupType = eval('startupType' + idHash);
	
	vfsImageGalleryInfo = {
		"fieldid": valId,
		"hashid": idHash,
		"imagepath": imagePath,
		"useformats": useFormats,
		"formatname": formatName,
		"formatvalue": formatValue,
		"scale": scale,
		"imgwidth": imgWidth,
		"imgheight": imgHeight,
		"editedresource": editedResource,
		"startupfolder": startupFolder,
		"startuptype": startupType
	};
}


/* Returns the value of the specified scale parameter. */
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

/* Returns the integer value of the specified scale parameter. */
function getScaleValueInt(scale, valueName) {
	try {
		return parseInt(getScaleValue(scale, valueName));
	} catch (e) {
		return 0;
	}
}

/* Removes the specified scale parameter value. */
function removeScaleValue(scale, valueName) {
	var pos = scale.indexOf(valueName + ":");
	if (pos != -1) {
		// found value, remove it from scale string
		var scalePrefix = "";
		if (pos > 0 && (valueName == "h" || valueName == "w")) {
			// special handling for "w" and "h", could also match "cw" and "ch"
			if (scale.charAt(pos - 1) == "c") {
				scalePrefix = scale.substring(0, pos + 1);
				scale = scale.substring(pos + 1);
			}
		}
		if (scale.indexOf(valueName + ":") != -1) {
			var searchVal = new RegExp(valueName + ":\\d+,*", "");
			scale = scale.replace(searchVal, "");	
		}
		scale = scalePrefix + scale;
	}	
	return scale;
}

/* Opens the image gallery popup window, dialog mode has to be "xml". */
function openVfsImageGallery(fieldId, idHash) {

	setVfsImageGalleryInfo(fieldId, idHash);
	var params = "?dialogmode=widget";
	params += "&params=" + JSON.stringify(vfsImageGalleryInfo);
	treewin = window.open(contextPath + vfsImageGalleryPath + params, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=650,height=700');
	
}

/* Checks is the preview button is enabled or disabled depending on the image value. */
function checkVfsImagePreview(fieldId) {
	var imgUri = document.getElementById("img." + fieldId).value;
	imgUri = imgUri.replace(/ /, "");
	if ((imgUri != "") && (imgUri.charAt(0) == "/")) {
		document.getElementById("preview" + fieldId).className = "show";
	} else {
		document.getElementById("preview" + fieldId).className = "hide";
	}
}

/* Opens a preview popup window to display the currently selected image. */
function previewVfsImage(fieldId, idHash) {
	setVfsImageGalleryInfo(fieldId, idHash);
	if ((vfsImageGalleryInfo.imagepath != "") && (vfsImageGalleryInfo.imagepath.charAt(0) == "/")) {
		var winWidth = 550;
		var winHeight = winWidth;
		var additionalScale = "";
		if (vfsImageGalleryInfo.imgwidth != "" && vfsImageGalleryInfo.imgwidth != "?") {
			winWidth = parseInt(vfsImageGalleryInfo.imgwidth) + 20;
			if (vfsImageGalleryInfo.imgheight != "" && vfsImageGalleryInfo.imgheight != "?") {
				additionalScale += ",w:" + parseInt(vfsImageGalleryInfo.imgwidth);
			}
		}
		if (vfsImageGalleryInfo.imgheight != "" && vfsImageGalleryInfo.imgheight != "?") {
			winHeight = parseInt(vfsImageGalleryInfo.imgheight) + 10;
			if (vfsImageGalleryInfo.imgwidth != "" && vfsImageGalleryInfo.imgwidth != "?") {
				additionalScale += ",h:" + parseInt(vfsImageGalleryInfo.imgheight);
			}
		}
		treewin = window.open(contextPath + vfsImageGalleryInfo.imagepath + "?__scale=" + vfsImageGalleryInfo.scale + additionalScale, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=' + winWidth + ',height=' + winHeight + '');
	}
}