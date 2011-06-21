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
 * - advancedVfsImageGalleryPath = "<%=CmsGallerySearchServer.ADVANCED_GALLERY_PATH%>";
 */

var advancedVfsImageGalleryPath;
var ade = {};
(function(ade) {
    
/* Triggered when the image format selector changes, removes the crop parameters if present. */
ade.setAdvancedImageFormat = function(valId, idHash) {    
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
			scale = removeAdvancedScaleValue(scale, "cx");
			scale = removeAdvancedScaleValue(scale, "cy");
			scale = removeAdvancedScaleValue(scale, "cw");
			scale = removeAdvancedScaleValue(scale, "ch");
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
		  		scale = removeAdvancedScaleValue(scale, "w");
		  		scale = removeAdvancedScaleValue(scale, "h");
		  		if (scale != "" && scale.charAt(scale.length - 1) != ",") {
		  			scale += ",";
		  		}
		  		scale += "w:" + widthInt + ",h:" + heightInt;
	  		}
	  	}
		scaleEl.value = scale;
	}
}

/* Returns the value of the specified scale parameter. */
function getAdvancedScaleValue(scale, valueName) {
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
function getAdvancedScaleValueInt(scale, valueName) {
	try {
		return parseInt(getAdvancedScaleValue(scale, valueName));
	} catch (e) {
		return 0;
	}
}

/* Removes the specified scale parameter value. */
function removeAdvancedScaleValue(scale, valueName) {
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

/**
 * Returns additional parameter for the image resource type.
 * 
 * @param {Object} fieldId the unique id of the input field of the current widget 
 * @param {Object} idHash unique id used for the name of js variables 
 */
var getAdvancedVfsImageGalleryInfo = function(fieldId, idHash) {
    var initialImageInfos = {};
    
    // get form elements	
	var formatNameEl = document.getElementById("format." + fieldId);
	var formatValueEl = document.getElementById("fmtval." + fieldId);
	var descEl = document.getElementById("desc." + fieldId);
	var scaleEl = document.getElementById("scale." + fieldId);
	var editedResource = "";
	try {
		editedResource = document.forms["EDITOR"].elements["resource"].value;
	} catch (e) {};
	
	// initialize values for object    
	var formatName = "";
	var formatValue = "";
	var desc = "";
	var scaleParam = "";
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
		scaleParam = scaleEl.value;
		if (formatValue != null && formatValue.length > 0) {
			// we have a format value, check scale parameters
			// for eventual width and height info and remove them if a format selector is present
			if (useFormats == true) {
				scaleParam = removeAdvancedScaleValue(scaleParam, "w");
				scaleParam = removeAdvancedScaleValue(scaleParam, "h");
			}
		} else {
			imgWidth = getAdvancedScaleValue(scaleParam, "w");
			imgHeight = getAdvancedScaleValue(scaleParam, "h");
		}
	}
        
    initialImageInfos['widgetmode'] = 'vfs';
    initialImageInfos['hashid'] = idHash;
    initialImageInfos['useformats'] = useFormats;
    initialImageInfos['formatname'] = formatName;
    initialImageInfos['formatvalue'] = formatValue;
    initialImageInfos['scale'] = scaleParam;
    initialImageInfos['imgwidth'] = imgWidth;
    initialImageInfos['imgheight'] =  imgHeight;
    initialImageInfos['editedresource'] = editedResource;
    
    return initialImageInfos;
}

/**
 * Returns the parameter for the request, combined from the image type and configuration.
 * 
 * @param {Object} fieldId the unique id of the input field of the current widget 
 * @param {Object} idHash unique id used for the name of js variables 
 */
var getAdvancedRequestDataInfo = function(fieldId, idHash) {
    var requestData = {};
	// selected path , if available
    var imageEl = document.getElementById("img." + fieldId);
    var selectedPath = imageEl.value;
    // startup param as string
    var startupFolder = eval('startupFolder' + idHash);
    // startup param as array    
    var startupFolders = eval('startupFolders' + idHash);
    
    // start type from the configuration could be 'gallery' or 'category'
    var startupType = eval('startupType' + idHash);
    var resourceTypes = eval('resourceTypes' + idHash);        
      
    // start tab id
    var startupTabId = eval('startupTabId' + idHash);
    // locale
    var locale = eval('locale' + idHash);
        
    var searchKeys = {
         'category': 'categories',
         'gallery': 'galleries'
    };
    
    // Json object as request parameter for standard part of the advanced vfs image          
    // if input field is not empty
    if (selectedPath) {
        requestData['resourcepath'] = selectedPath;
        requestData['types'] = resourceTypes;
    // if input field not empty
    } else {
         requestData = {
            'querydata': {},
            'types': resourceTypes
         };
        requestData['querydata']['types'] = resourceTypes;
        requestData['querydata']['galleries'] = [];
        requestData['querydata']['categories'] = [];
        requestData['querydata']['matchesperpage'] = 12;
        requestData['querydata']['query'] = '';         
        requestData['querydata']['page'] = 1;
        requestData['querydata']['locale'] = locale;
        // check the startup parameter
        if (startupFolder != null) {
            requestData['querydata'][searchKeys[startupType]] = [startupFolder];
            requestData['querydata']['tabid'] = 'cms_tab_results';
        } else if (startupFolders != null) {
            requestData['querydata'][searchKeys[startupType]] = startupFolders;
            requestData['querydata']['tabid'] = 'cms_tab_results';
        } else {
            requestData['querydata']['tabid'] = startupTabId;
        }   
    }       
    return requestData;
}

/* Opens the image gallery popup window, dialog mode has to be "xml". */
ade.openAdvancedVfsImageGallery = function(dialogMode, fieldId, idHash) {       
    var requestData = getAdvancedRequestDataInfo(fieldId, idHash);
    var initialImageInfos = getAdvancedVfsImageGalleryInfo(fieldId, idHash);
    // tabs to be displayed
    var galleryTabs = eval('galleryTabs' + idHash);
     
    // set the parameter string for the default part
	var paramString = "?dialogmode=" + dialogMode;
    paramString += "&fieldid=" + fieldId;
    paramString += "&tabs=" + JSON.stringify(galleryTabs);
    paramString += "&path=" + requestData['resourcepath'];
    paramString += "&data=" + JSON.stringify(requestData);
    paramString += "&imagedata=" + JSON.stringify(initialImageInfos);
	treewin = window.open(contextPath + advancedVfsImageGalleryPath + paramString, "opencms", 'toolbar=no,location=no,directories=no,status=yes,resizable=yes,top=20,left=150,width=660,height=510');
	
}

/* Checks is the preview button is enabled or disabled depending on the image value. */
ade.checkAdvancedVfsImagePreview = function(fieldId) {
	var imgUri = document.getElementById("img." + fieldId).value;
	imgUri = imgUri.replace(/ /, "");
	if ((imgUri != "") && (imgUri.charAt(0) == "/")) {
		document.getElementById("preview" + fieldId).className = "show";
	} else {
		document.getElementById("preview" + fieldId).className = "hide";
	}
}

/* Opens a preview popup window to display the currently selected image. */
ade.previewAdvancedVfsImage = function(fieldId, idHash) {
	var initialImageInfos =  getAdvancedVfsImageGalleryInfo(fieldId, idHash);
    // selected path, if available
    var imageEl = document.getElementById("img." + fieldId);
    var imagepath = imageEl.value;
	if ((imagepath != "") && (imagepath.charAt(0) == "/")) {
		var winWidth = 550;
		var winHeight = winWidth;
		var additionalScale = "";
		if (initialImageInfos.imgwidth != "" && initialImageInfos.imgwidth != "?") {
			winWidth = parseInt(initialImageInfos.imgwidth) + 20;
			if (initialImageInfos.imgheight != "" && initialImageInfos.imgheight != "?") {
				additionalScale += ",w:" + parseInt(initialImageInfos.imgwidth);
			}
		}
		if (initialImageInfos.imgheight != "" && initialImageInfos.imgheight != "?") {
			winHeight = parseInt(initialImageInfos.imgheight) + 10;
			if (initialImageInfos.imgwidth != "" && initialImageInfos.imgwidth != "?") {
				additionalScale += ",h:" + parseInt(initialImageInfos.imgheight);
			}
		}
		treewin = window.open(contextPath + imagepath + "?__scale=" + initialImageInfos.scale + additionalScale, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=' + winWidth + ',height=' + winHeight + '');
	}
}

})(ade);

var setAdvancedImageFormat = ade.setAdvancedImageFormat;
var previewAdvancedVfsImage = ade.previewAdvancedVfsImage;
var checkAdvancedVfsImagePreview = ade.checkAdvancedVfsImagePreview;
var openAdvancedVfsImageGallery = ade.openAdvancedVfsImageGallery;