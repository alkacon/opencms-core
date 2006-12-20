<%@ page import="org.opencms.jsp.*, org.opencms.editors.fckeditor.*, org.opencms.loader.*, org.opencms.main.*, org.opencms.workplace.*, org.opencms.workplace.galleries.*" buffer="none" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsDialog wp = new CmsDialog(cms);

%>/* absolute path to JSP that delivers the (AJAX) requested data from the server */
var vfsPathGalleryJsp = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/gallery/gallery.jsp") %>";

/* absolute path to plugin */
var vfsPathPlugin = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/") %>";

/* absolute path to the JSP that displays the image in original size */
var vfsPopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";

/* context path and workplace server name */
var ocmsContext = "<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";
var ocmsServer = "<%= OpenCms.getSiteManager().getWorkplaceServer() %>";

/* parameter name of the __scale request parameter */
var paramScale = "<%= CmsImageScaler.PARAM_SCALE %>";

/* holds the gallery images that are shown */
var gItems= new Array();

/* the current page for displaying gallery items */
var currentPage = 1;

/* the currently active image */
var activeImage = null;

/* checks the request state and returns the current state text */
function checkRequestState(state) {
	var txt = "";
	if (state == "fatal") {
		txt = "<%= wp.key(org.opencms.editors.fckeditor.Messages.GUI_AJAX_STATE_GIVEUP_0) %>";
	} else if (state == "wait") {
		txt = GetE("imgAjaxWait").innerHTML;
	} else if (state == "error") {
		txt = "<%= wp.key(org.opencms.editors.fckeditor.Messages.GUI_AJAX_STATE_ERROR_0) %> " + msg;
	}
	return txt;
}

/* shows the gallery select box */
function showGallerySelectBox() {
	makeRequest(vfsPathGalleryJsp + "?<%= CmsDialog.PARAM_ACTION %>=<%= CmsFCKEditorDialogImage.DIALOG_GETGALLERIES %>", "getGalleries");
}

/* generates the html to show for the gallery selection */
function getGalleries(msg, state) {
	var elem = document.getElementById("galleryselect");
	if (state != "ok") {
		elem.innerHTML = checkRequestState(state);
	} else {
		elem.innerHTML = msg;
   	}
}

/* shows the gallery items depending on the selected gallery */
function showGalleryItems() {
	var elem = document.forms["galleryform"].elements[0];
	if (elem != null) {
		var val = elem.value;
		makeRequest(vfsPathGalleryJsp + "?<%= A_CmsGallery.PARAM_GALLERYPATH %>=" + val +"&<%= CmsDialog.PARAM_ACTION %>=<%= A_CmsGallery.DIALOG_LIST %>", "getGalleryItems");
	} else {
		setTimeout("showGalleryItems()", 500);
	}
}

/* needed for compatibility reasons to the common gallery, it's called on the onchange event of the gallery select box */
function displayGallery() {
	showGalleryItems();
}

/* generates the html to display the gallery items */
function getGalleryItems(msg, state) {
	var elem = document.getElementById("galleryitems");
	if (state != "ok") {
		elem.innerHTML = checkRequestState(state);
	} else {
		gItems= new Array();
		eval(msg);
 		buildHtmlGalleryItems(1);
   	}
}

/* returns the currently active image */
function getActiveImage() {
	return activeImage;
}

/* sets the currently active image */
function setActiveImage(imgIndex, imgUrl) {
	if (activeImage != null) {
		// reset border of old active image in gallery view
		var oldElem = document.getElementById(activeImage.structureId);
		if (oldElem != null) {
			oldElem.className = "galleryitem";
		}
	}
	if (imgIndex != -1) {
		// index given, call comes from gallery item click
		activeImage = gItems[imgIndex];
		// set border of new active image
		var newElem = document.getElementById(activeImage.structureId);
		newElem.className = "galleryitemactive";
		SetUrl(activeImage.url, activeImage.width, activeImage.height);
		updateImageInfo();
	} else {
		// no index given, call comes from UpdateImage function (use timeout to avoid Mozilla AJAX problems!)
		setTimeout("makeRequest('" + vfsPathGalleryJsp + "?<%= CmsFCKEditorDialogImage.PARAM_IMGURL %>=" + imgUrl +"&<%= CmsDialog.PARAM_ACTION %>=<%= CmsFCKEditorDialogImage.DIALOG_GETACTIVEIMAGE %>', 'getActiveImageFromServer');", 0);
	}
}

/* sets the currently active image from OpenCms */
function getActiveImageFromServer(msg, state) {
	if (state == "ok") {
		if (msg != null && msg != "<%= CmsFCKEditorDialogImage.RETURNVALUE_NONE %>") {
			eval(msg);
			var newElem = document.getElementById(activeImage.structureId);
			if (newElem != null) {
				// mark gallery item as active
				newElem.className = "galleryitemactive";
			}
			updateImageInfo();
		}
   	}
}

/* updates the image information for the selected image */
function updateImageInfo() {
	showGalleryImageInfo(-1, "detailimage", activeImage, true);
}


/* generates the gallery item view */
function buildHtmlGalleryItems(displayPage) {
	var result = "";
	currentPage = displayPage;
	var rowCount = 3;
	var columns = 4;
	var photosPerPage = rowCount * columns;
	var startIndex = (currentPage - 1) * photosPerPage;
        var endIndex = 0;
        
        // calculate end index
        endIndex = (currentPage * photosPerPage) - 1;
        if (endIndex > (gItems.length - 1)) {
            endIndex = gItems.length - 1;
        }

        result += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"";
        result += "  class=\"galleryarea\">\n";

        var imgIndex = startIndex;
        for (var i = 1; i <= rowCount; i++) {
            // build the table thumbnail rows
            result += "<tr>\n";
            for (var k = 1; k <= columns; k++) {
                // build the thumbnail table data cell
                result += "\t<td class=\"galleryitem";
		if (imgIndex <= endIndex) {
			// current image is in list range, show it
			var currImg= gItems[imgIndex];
			if (activeImage != null && activeImage.structureId == currImg.structureId) {
				result += "active";
			}
			result += "\" id=\"";
			result += currImg.structureId;
			result += "\">";
			// show state information for new or changed image
			if (currImg.state == 1 || currImg.state == 2) {
				//result += "\" onmouseover=\"document.getElementById(\'st_" + currImg.structureId + "\').style.display = 'block'\" onmouseout=\"document.getElementById(\'st_" + currImg.structureId + "\').style.display = 'none'\">";
				result += "<div class=\"imglayer\" id=\"st_" + currImg.structureId + "\">";
				result += "<img src=\"<%= cms.link("/system/workplace/resources/commons/warning.png") %>\" title=\"";
				if (currImg.state == 1) {
					// changed image
					result += GetE("imgStateChanged").innerHTML;
				} else {
					// new image
					result += GetE("imgStateNew").innerHTML;
				}
				result += "\" border=\"0\">";
				result += "</div>";
			}

			// create the link to the detail view
			result += "<a href=\"javascript:setActiveImage(";
			result += imgIndex;
			result += ");\">";
			// create the scaled thumbnail
			result += "<img src=\"";
			result += currImg.link;
			result += "\" border=\"0\"";
			result += " alt=\"";
			result += currImg.title;
			result += "\" title=\"";
			result += currImg.title;
			result += "\" onmouseover=\"showGalleryImageInfo(";
			result += imgIndex;
			result += ");\" onmouseout=\"hideGalleryImageInfo();";
			result += "\">";
			result += "</a>";
			// show additional information
			result += "<br/>";
			result += currImg.width;
			result += " x ";
			result += currImg.height;
			result += " (";
			result += currImg.size;
			result += ")";
			imgIndex++;
		} else {
			result += "\">";
			result += "&nbsp;";
		}
                result += "</td>\n";
	    }
	    result += "</tr>\n";
	}

	result += "<tr><td class=\"gallerynavigation\" colspan=\"";
	result += columns + "\">";

	var pageCount = Math.ceil(gItems.length / photosPerPage);
	if (pageCount > 1) {
		// show navigation and number of pages greater than 1 
		result += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tr><td>";    
		if (currentPage > 1) {
			// build the "Back to start" link
			result += "<a href=\"javascript:buildHtmlGalleryItems(1);\"><img src=\"";
			result += vfsPathPlugin + "buttons/nav_beginning.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";
			result += "</a> ";
			// build the "Back" link
			result += "<a href=\"javascript:buildHtmlGalleryItems(" + (currentPage - 1) + ");\"><img src=\"";
			result += vfsPathPlugin + "buttons/nav_step_back.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";
			result += "</a>";
		} else {
			// build the "Back to start" image
			result += "<img src=\"";
			result += vfsPathPlugin + "buttons/nav_beginning_i.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\"> ";
			// build the "Back" image
			result += "<img src=\"";
			result += vfsPathPlugin + "buttons/nav_step_back_i.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";
		}
		result += "</td><td>&nbsp;&nbsp;</td>";
		result += "<td style=\"vertical-align: top; line-height: 16px;\">";
		// build the page index information
		var displayPages = 15;
		var countBeforeCurrent = Math.floor(displayPages / 2);
            	var countAfterCurrent;
		if ((currentPage - countBeforeCurrent) < 1) {
			// set count before to number of available pages 
			countBeforeCurrent = currentPage - 1;
		}
		// set count after to number of remaining pages (- 1 for current page) 
		countAfterCurrent = displayPages - countBeforeCurrent - 1;
		// calculate start and end index
		startIndex = currentPage - countBeforeCurrent;
		endIndex = currentPage + countAfterCurrent;
		// check end index
		if (endIndex > pageCount) {
			var delta = endIndex - pageCount;
			// decrease start index with delta to get the right number of displayed pages
			startIndex -= delta;
			// check start index to avoid values < 1
			if (startIndex < 1) {
				startIndex = 1;
			}
			endIndex = pageCount;
		}
		for (var i = startIndex; i <= endIndex; i++) {
			if (i == currentPage) {
				result += "<span class=\"gallerypageactive\">";
				result += i + " </span>";
			} else {
				result += "<a href=\"javascript:buildHtmlGalleryItems(" + i + ");\" class=\"gallerypage\">";
				result += i;
				result += "</a> ";
			}
		}
		result += "</td><td>&nbsp;&nbsp;</td>";
		result += "<td>";
		if (currentPage < pageCount) {
			// build the "Next" link
			result += "<a href=\"javascript:buildHtmlGalleryItems(" + (currentPage + 1) + ");\"><img src=\"";
			result += vfsPathPlugin + "buttons/nav_step_forward.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";
			result += "</a> ";
			// build the "to end" link
			result += "<a href=\"javascript:buildHtmlGalleryItems(" + pageCount + ");\"><img src=\"";
			result += vfsPathPlugin + "buttons/nav_end.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";
			result += "</a>";
		} else {
			// build the "Next" image
			result += "<img src=\"";
			result += vfsPathPlugin + "buttons/nav_step_forward_i.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\"> ";
			// build the "to end" image
			result += "<img src=\"";
			result += vfsPathPlugin + "buttons/nav_end_i.png";
			result += "\" border=\"0\" alt=\"\" width=\"16\" height=\"16\">";

		}
		result += "</td></tr></table>";
	} else {
		result += "<span class=\"gallerypageactive \">&nbsp;</span>";
	}

	result += "</td></tr>\n";
	result += "</table>";

	var elem = document.getElementById("galleryitems");
	elem.innerHTML = result;
	oEditor.FCKLanguageManager.TranslateElements(document, "SPAN", "innerHTML") ;
}

/* stores the hide image info timeout to interrupt it on a mouseover */
var imgDetailTimeout;

/* shows the additional image information (called on mouseover) */
function showGalleryImageInfo(imgIndex, idPrefix, currImg, showAll) {
	clearTimeout(imgDetailTimeout);
	if (imgIndex != -1 && currImg == null) {
		currImg = gItems[imgIndex];
	}
	if (idPrefix == null) {
		idPrefix = "galleryimage";
	}
	var imgName = "<span title=\"";
	imgName += currImg.url;
	imgName += "\">";
	imgName += currImg.url.substring(currImg.url.lastIndexOf("/") + 1);
	imgName += "</span>";
	var stateTxt = "";
	if (currImg.state == 1) {
		// changed image
		stateTxt = GetE("imgStateChanged").innerHTML;
		GetE(idPrefix + "state").className = "stateinfochanged";
	} else if (currImg.state == 2) {
		// new image
		stateTxt = GetE("imgStateNew").innerHTML;
		GetE(idPrefix + "state").className = "stateinfonew";
	}
	GetE(idPrefix + "state").innerHTML = stateTxt;
	GetE(idPrefix + "name").innerHTML = imgName;
	GetE(idPrefix + "title").innerHTML = currImg.title;
	GetE(idPrefix + "type").innerHTML = getImageType(currImg.type);
	GetE(idPrefix + "dc").innerHTML = currImg.dateCreated;
	GetE(idPrefix + "dm").innerHTML = currImg.dateModified;
	GetE(idPrefix + "id").innerHTML = currImg.structureId;
	if (showAll != null && showAll == true) {
		GetE(idPrefix + "format").innerHTML = currImg.width + " x " + currImg.height;
		GetE(idPrefix + "size").innerHTML = currImg.size;
		if (GetE("txtAlt").value == "") {
			GetE("txtAlt").value = currImg.title;
		}
	}
	
}

/* hides the additional image information delayed (called on mouseout) */
function hideGalleryImageInfo() {
	imgDetailTimeout = setTimeout("doHideGalleryImageInfo()", 350);
}

/* hides the additional image information */
function doHideGalleryImageInfo() {
	GetE("galleryimagestate").innerHTML = "";
	GetE("galleryimagename").innerHTML = "";
	GetE("galleryimagetitle").innerHTML = "";
	GetE("galleryimagetype").innerHTML = "";
	GetE("galleryimagedc").innerHTML = "";
	GetE("galleryimagedm").innerHTML = "";
	GetE("galleryimageid").innerHTML = "";
}

/* ######### General dialog helper methods ######### */

function setAltText() {
	GetE("txtAlt").value = activeImage.title;
}

function setCopyrightText() {
	var copyText = activeImage.copyright;
	if (copyText == null) {
		copyText = "";
	} else {
		copyText = "&copy; " + copyText;
	}
	GetE("txtCopyrightText").value = copyText;
}

function setImageBorder() {
	if (insertImageBorder()) {
		var hSp = GetE("txtHSpace").value;
		if (hSp == "") {
			GetE("txtHSpace").value = "5";
		}
		var vSp = GetE("txtVSpace").value;
		if (vSp == "") {
			GetE("txtVSpace").value = "5";
		}
	} else {
		GetE("txtHSpace").value = "";
		GetE("txtVSpace").value = "";
	}
}

function setSize(dimension, val) {
	if (val == null || val == "") {
		return;
	}
	if (dimension == "Height") {
		GetE("txtHeight").value = val;
		GetE("txtWidthPreset").selectedIndex = 0;
	} else {
		GetE("txtWidth").value = val;
		GetE("txtHeightPreset").selectedIndex = 0;
	}
	OnSizeChanged(dimension,val);
}

/* determines the image type, e.g. gif or jpg image */
function getImageType(imgSuffix) {
	if (imgSuffix == null || imgSuffix .length == 0) {
		return "";
	}
	if (imgSuffix == "gif") {
		return FCKLang.DlgImgDetailTypegif;
	}
	if (imgSuffix == "jpg" || imgSuffix == "jpeg") {
		return FCKLang.DlgImgDetailTypejpg;
	}
	if (imgSuffix == "png") {
		return FCKLang.DlgImgDetailTypepng;
	}
	if (imgSuffix == "tif" || imgSuffix == "tiff") {
		return FCKLang.DlgImgDetailTypetif;
	}
	if (imgSuffix == "bmp") {
		return FCKLang.DlgImgDetailTypebmp;
	}
	return FCKLang.DlgImgDetailTypeNo; 
}

function insertSubTitle() {
	return checkChecked("txtSubtitle");
}

function insertCopyright() {
	return checkChecked("txtCopyright");
}

function insertLinkToOriginal() {
	return checkChecked("txtLinkOriginal");
}

function insertImageBorder() {
	return checkChecked("txtImageBorder");
}


function checkChecked(elemName) {
	var elem = GetE(elemName);
	if (elem) {
		return elem.checked;
	}
	return false;
}

/* ######### Definition of objects usable in dialog and methods to create them ######### */

/* represents an image file to display in the gallery */
var ImgFile = function(sitePath, url, link, title, width, height, size, dateCreated, dateModified, structureId, type, hashCode, state, copyright) {
	this.sitePath = sitePath;
	this.url = url;
	this.link = link;
	this.title = title;
	this.width = width;
	this.height = height;
	this.size = size;
	this.dateCreated = dateCreated;
	this.dateModified = dateModified;
	this.structureId = structureId;
	this.type = type;
	this.hashCode = hashCode;
	this.state = state;
	this.copyright = copyright;
}

/* represents an image scaler to use */
var OCmsScaler = function(width, height, type, color, quality) {
	this.width = width;
	this.height = height;
	this.type = 4;
	if (type != null) {
		this.type = type;
	}
	this.color = "#FFFFFF";
	if (color != null) {
		this.color = color;
	}
	this.quality = -1;
	if (quality != null) {
		this.quality = quality;
	}

}

/* image scaler method to get the members as vails request parameter string */
OCmsScaler.prototype.toParams = function() {
	var result = "?";
	result += paramScale;
	result += "="
	result += "w:";
	result += this.width;
	result += ",h:";
	result += this.height;
	result += ",t:";
	result += this.type;
	result += ",c:";
	if (this.color.charAt(0) == "#") {
		result += this.color.substring(1);
	} else {
		result += this.color;
	}
	if (this.quality > -1) {
		result += ",q:";
		result += this.quality;
	}
	return result;
}

/* returns an initialized scaler object generated from the given parameter */
function createScaler(paramValue) {
	var width = getScalerSettingFromString("w", paramValue);
	var height = getScalerSettingFromString("h", paramValue);
	var type = getScalerSettingFromString("t", paramValue);
	var col = getScalerSettingFromString("c", paramValue);
	if (col != null) {
		col = "#" + col;
	}
	var q = getScalerSettingFromString("q", paramValue);
	return new OCmsScaler(width, height, type, col, q);
}

/* helper method that returns a scaler setting value from a parameter string */
function getScalerSettingFromString(setting, paramValue) {
	var checkValue = paramValue;
	var begin = setting + ":";
	var settingStart = checkValue.indexOf(begin);
	if (settingStart != -1) {
		checkValue = checkValue.substring(settingStart + begin.length);
		var settingEnd = checkValue.indexOf(",");
		if (settingEnd != -1) {
			checkValue = checkValue.substring(0, settingEnd);
		}
		return checkValue;
	}
	return null;
}
