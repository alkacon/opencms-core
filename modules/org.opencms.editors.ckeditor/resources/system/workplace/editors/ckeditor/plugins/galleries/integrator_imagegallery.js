<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

String editedResource = "";
if (CmsStringUtil.isNotEmpty(wp.getParamResource())) {
	editedResource = wp.getParamResource();
}

%>/* Initialize important CKEditor variables from editor. */
var editorName 	= window.parent.dialogEditorInstanceName;
var CKEDITOR	= window.parent.CKEDITOR;
var oEditor	= CKEDITOR.instances[editorName];

/* Event listener to be triggered when pressing the "ok" button. */
var okListener = function(ev) {
	// call Ok() with parameters, otherwise the variables will be null in IE
	Ok(oImage, oLink, oSpan);
        // remove the listeners to avoid any JS exceptions
        CKEDITOR.dialog.getCurrent().removeListener("ok", okListener);
	CKEDITOR.dialog.getCurrent().removeListener("cancel", cancelListener);
};

/* Event listener to be triggered when pressing the "cancel" button. */
var cancelListener = function(ev) {
        // remove the listeners to avoid any JS exceptions
        CKEDITOR.dialog.getCurrent().removeListener("ok", okListener);
	CKEDITOR.dialog.getCurrent().removeListener("cancel", cancelListener);
};

/* Register event listeners for "ok" and "cancel" buttons. */
CKEDITOR.event.implementOn(CKEDITOR.dialog.getCurrent());
CKEDITOR.dialog.getCurrent().on("ok", okListener);
CKEDITOR.dialog.getCurrent().on("cancel", cancelListener);

/* Enables or disables the enhanced image dialog options. */
var showEnhancedOptions = false;
var useTbForLinkOriginal = false;

/* The selected image (if available). */
var oImage = null;

/* The active link. */
var oLink = null;

/* The span around the image, if present. */
var oSpan = null;

/* Absolute path to the JSP that displays the image in original size. */
var vfsPopupUri = "<%= wp.getJsp().link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";

/* Size of the preview area. */
previewX = 600;
previewY = 230;

imagesPerPage = 12;

/* Initialize the dialog values. */
initValues = {};
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = false;
initValues.editedresource = "<%= editedResource %>";

/* Initializes the image gallery popup window. */
function initPopup() {
	if (showEnhancedOptions == true) {
		// show additional fields in enhanced mode
		$("#enhAltCheck").show();
		$("#enhAltBt").show();
		$("#enhCopy").show();
		$("#enhOrig").show();
	} else {
		// common mode, hide enhanced options and enlarge preview area
		$("#enhAltCheck").hide();
		$("#enhAltBt").hide();
		$("#enhCopy").hide();
		$("#enhOrig").hide();
		previewY = 270;
		$("#previewwrapper").height(270);
		$("#imgoptions").height(108);
	}
	// load eventually selected image and information
	loadSelection();
	$("#btbrowseserver").hide();
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();
	if (initValues.itempath != null && initValues.itempath != "") {
		$.post(vfsPathAjaxJsp, { action: "getactiveitem", itempath: initValues.itempath}, function(data){ loadActiveItem(data, true); });
	} else {
		$tabs.tabs("select", 1);
		$tabs.tabs("disable", 0);
		$tabs.tabs("disable", 3);
	}
	// load galleries and categories
	setTimeout("getGalleries();", 50);
	setTimeout("getCategories();", 100);
}

/* Do additional stuff when active image is loaded. */
function activeImageAdditionalActions(isInitial) {
	if (!isInitial == true) {
		resetCopyrightText();
		var imgTitle = activeItem.title;
		if (activeItem.description != "") {
			imgTitle = activeItem.description;
		}
		elById("txtAlt").value = imgTitle;
	}
}

/* Opens the file browser popup for the link dialog. */
function LnkBrowseServer() {
        OpenFileBrowser(FCKConfig.LinkBrowserURL, FCKConfig.LinkBrowserWindowWidth, FCKConfig.LinkBrowserWindowHeight);
}

/* Triggered by the file browser popup to set the selected URL in the input field. */
function SetUrl( url, width, height, alt ) {
        elById("txtLnkUrl").value = url;
}

/* Loads the selected image from the editor, if available. */
function loadSelection() {
	
	// get the selection
	var selection = oEditor.getSelection();
	// get the selected image
	oImage = selection.getSelectedElement();
	if (oImage && oImage.getName() != "img" && oImage.getName() != "span" && !(oImage.getName() == "input" && oImage.getAttribute("type") == "image")) {
		oImage = null;
	}
	// get the active link
	oLink = oImage && selection.getSelectedElement().getAscendant("a");

	if (!oImage) {
		// no image selected, nothing to do...
		elById('cmbAlign').value = "left";
		elById("txtHSpace").value = "5";
		elById("txtVSpace").value = "5";
		elById("imageBorder").checked = true;
		return;
	}

	var altText = "";
	var copyText = "";
	var imgBorder =	false;
	var imgHSp = "";
	var imgVSp = "";
	var imgAlign = getAttr(oImage, "align", "");
	if (selection.getSelectedElement().hasAscendant("span") || selection.getSelectedElement().hasAscendant("table")) {
		if (selection.getSelectedElement().hasAscendant("span")) {
			oSpan =	selection.getSelectedElement().getAscendant("span");
		} else {
			oSpan =	selection.getSelectedElement().getAscendant("table");
		}
		try {
			var idPart = getAttr(oSpan, "id", "").substring(1);
			if (idPart == getAttr(oImage, "id", "").substring(1)) {

				var altElem = oEditor.document.getById("s" + idPart);
				if (altElem) {
					altText	= altElem.getFirst().getText();
					elById("insertAlt").checked = true;
				}

				var cpElem = oEditor.document.getById("c" + idPart);
				if (cpElem) {
					copyText = cpElem.getFirst().getText();
					elById("insertCopyright").checked = true;
				}
				var divElem = oEditor.document.getById("a" + idPart);
				imgHSp = divElem.getStyle("marginLeft");
				if (imgAlign == "left") {
			 		imgHSp = divElem.getStyle("marginRight");
			 	} else if (imgAlign == "right") {
			 		imgHSp = divElem.getStyle("marginLeft");
			 	}
				imgVSp = divElem.getStyle("marginBottom");
			}
		} catch	(e) {}
	 } else	{
	 	if (imgAlign == "left") {
	 		imgHSp = oImage.getStyle("marginRight");
			imgVSp = oImage.getStyle("marginBottom");
			if (imgHSp == "") {
				imgHSp = getAttr(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = getAttr(oImage, "vspace", "");
			}
	 	} else if (imgAlign == "right") {
	 		imgHSp = oImage.getStyle("marginLeft");
	 		imgVSp = oImage.getStyle("marginBottom");
	 		if (imgHSp == "") {
				imgHSp = getAttr(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = getAttr(oImage, "vspace", "");
			}
	 	} else {
			imgHSp = getAttr(oImage, "hspace", "");
			imgVSp = getAttr(oImage, "vspace", "");
		}
	}
	var cssTxt = getAttr(oImage, "style", "");
	if (showEnhancedOptions) {
		if (imgAlign == "left") {
			cssTxt = cssTxt.replace(/margin-right:\s*\d+px;/, "");
			cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
	 	} else if (imgAlign == "right") {
			cssTxt = cssTxt.replace(/margin-left:\s*\d+px;/, "");
			cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
	 	}
 	}
	
	if (altText == "") {
		altText	= getAttr(oImage, "alt", "");
	}

	var sUrl = getAttr(oImage, "_cke_saved_src", null);
	if (sUrl == null) {
		sUrl = getAttr(oImage, "src", "");
	}
	var paramIndex = sUrl.indexOf("?__scale");
	if (paramIndex != -1) {
		initValues.scale = sUrl.substring(paramIndex + 9);
		sUrl = sUrl.substring(0, paramIndex);
	}

	initValues.itempath = sUrl;

	elById("txtAlt").value = altText;
	if (copyText !=	"")	{
		elById("txtCopyright").value = copyText;
	}
	
	if (isNaN(imgHSp) && imgHSp.indexOf("px") != -1)	{	
		imgHSp = imgHSp.substring(0, imgHSp.length - 2);
	}
	if (isNaN(imgVSp) && imgVSp.indexOf("px") != -1)	{	
		imgVSp = imgVSp.substring(0, imgVSp.length - 2);
	}
	
	if (imgHSp != "") {
		var test = parseInt(imgHSp);
		if (isNaN(test) || test < 1) {
			imgHSp = "";
		}
	}
	
	if (imgVSp != "") {
		var test = parseInt(imgVSp);
		if (isNaN(test) || test < 1) {
			imgVSp = "";
		}
	}
	
	if (imgHSp != "" || imgVSp != "") {
		elById("txtVSpace").value = imgVSp;
		elById("txtHSpace").value = imgHSp;
		elById("imageBorder").checked = true;
	}

	elById("cmbAlign").value = imgAlign;

	var iWidth, iHeight;

	var regexSize = /^\s*(\d+)px\s*$/i ;

	if (oImage.getStyle("width"))	{
		var aMatch = oImage.getStyle("width").match(regexSize);
		if (aMatch) {
			iWidth = aMatch[1];
			oImage.setStyle("width", "");
		}
	}

	if (oImage.getStyle("height")) {
		var aMatch = oImage.getStyle("height").match(regexSize);
		if (aMatch) {
			iHeight = aMatch[1];
			oImage.setStyle("height", "");
		}
	}

	iWidth = iWidth ? iWidth : getAttr(oImage, "width", "");
	iHeight = iHeight ? iHeight : getAttr(oImage, "height", "");

	initValues.imgwidth = "" + iWidth;
	initValues.imgheight = "" + iHeight;

	// get Advanced	Attributes
	elById("txtAttId").value = getAttr(oImage, "id", "");
	elById("cmbAttLangDir").value = getAttr(oImage, "dir", "");;
	elById("txtAttLangCode").value = getAttr(oImage, "lang", "");;
	elById("txtAttTitle").value = getAttr(oImage, "title", "");;
	elById("txtAttClasses").value = getAttr(oImage, "class", "");
	elById("txtLongDesc").value = getAttr(oImage, "longdesc", "");;
	elById("txtAttStyle").value = cssTxt;

	if (oLink) {
		var lnkUrl = getAttr(oLink, "_cksavedurl", null);
		if (lnkUrl == null) {
			lnkUrl = getAttr(oLink, "href", "");
		}
		if (lnkUrl != sUrl) {
			elById("txtLnkUrl").value = lnkUrl;
			elById("cmbLnkTarget").value = getAttr(oLink, "target", "");
		}
		var idAttr = getAttr(oLink, "id", null);
		if (idAttr != null && idAttr.indexOf("limg_") == 0) {
			elById("linkOriginal").checked = true;
		}
	}
}

/* Resets the image alternative text to the original value. */
function resetAltText() {
	var imgTitle = activeItem.title;
	if (activeItem.description != "") {
		imgTitle = activeItem.description;
	}
	elById("txtAlt").value = imgTitle;
}

/* Resets the image copyright text to the original value. */
function resetCopyrightText() {
	var copyText = activeItem.copyright;
	if (copyText == null || copyText == "") {
		copyText = "";
	} else {
		copyText = "&copy; " + copyText;
	}
	elById("txtCopyright").value = copyText;
}

/* Toggles the image spacing values. */
function setImageBorder() {
	if (insertImageBorder()) {
		var hSp = elById("txtHSpace").value;
		if (hSp == "") {
			elById("txtHSpace").value = "5";
		}
		var vSp = elById("txtVSpace").value;
		if (vSp == "") {
			elById("txtVSpace").value = "5";
		}
	} else {
		elById("txtHSpace").value = "";
		elById("txtVSpace").value = "";
	}
}

/* Returns if the image border checkbox is checked or not. */
function insertImageBorder() {
	return checkChecked("imageBorder");
}

/* Returns if the link to original image checkbox is checked or not. */
function insertLinkToOriginal() {
	return checkChecked("linkOriginal");
}

/* Returns if the sub title checkbox is checked or not. */
function insertSubTitle() {
	return checkChecked("insertAlt");
}

/* Returns if the copyright checkbox is checked or not. */
function insertCopyright() {
	return checkChecked("insertCopyright");
}

/* Helper method to determine if a checkbox is checked or not. */
function checkChecked(elemName) {
	var elem = elById(elemName);
	if (elem) {
		return elem.checked;
	}
	return false;
}

/* Returns if enhanced options are used and sub title or copyright should be inserted. */
function isEnhancedPreview() {
	return showEnhancedOptions && (insertSubTitle() || insertCopyright());
}

/* The OK button was hit, called by editor button click event. */
function Ok(eImage, eLink, eSpan) {
	if (activeItem == null) {
		// no image selected, nothing to do
		return;
	}

	var imgCreated = false;
	if (eImage == null) {
		eImage = oEditor.document.createElement("img");
		// set flag that image is newly created
		imgCreated = true;
	}
	
	updateImage(eImage);

	// now its getting difficult, be careful when modifying anything below this comment...

	if (isEnhancedPreview() && eLink) {
		// original link has to be removed if a span is created in enhanced options
		eLink.remove(true);
	}

	// now we set the image object either to the image or in case of enhanced options to a span element
	eImage = createEnhancedImage(eImage, eLink, eSpan);

	if (showEnhancedOptions && eSpan != null && (getAttr(eSpan, "id", "").substring(0, 5) == "aimg_" || getAttr(eSpan, "id", "").substring(0, 5) == "timg_")) {
		// span is already present, select it
		oEditor.getSelection().selectElement(eSpan);
		// remove child elements of span
		if (eSpan.getFirst() != null) {
			eSpan.getFirst().remove(false);
		}
	}

	if (eImage.getName()!= "span") {

		// the object to insert is a simple image, check the link to set
		var imgLink = null;
		if (!imgCreated && oEditor.getSelection().getSelectedElement() != null) {
			imgLink = oEditor.getSelection().getSelectedElement().getAscendant("a", true);
		}
		var sLnkUrl = trimStr(elById("txtLnkUrl").value);
		var linkOri = "";

		if (insertLinkToOriginal()) {
			sLnkUrl = "#";
			linkOri = getLinkToOriginal();
		} else if (sLnkUrl == "#") {
			sLnkUrl = "";
		}
		if (sLnkUrl.length == 0) {
			if (imgLink) {
				// remove existing link
				imgLink.remove(true);
				oEditor.getSelection().selectElement(eImage);
			}
		} else {
			var linkPresent = true;
			if (!imgLink) {  
				// creating a new link
				imgLink = oEditor.document.createElement("a");
				linkPresent = false;
			}
			setAttr(imgLink, "href", sLnkUrl);			

			if (linkOri != "") {
				// set the necessary attributes for the link to original image
				try {
					if (useTbForLinkOriginal == true) {
						setAttr(imgLink, "href", linkOri);
						setAttr(imgLink, "title", elById("txtAlt").value);
						setAttr(imgLink, "class", "thickbox");
						sLnkUrl = linkOri;
					} else {
						setAttr(imgLink, "onclick", linkOri);
					}
					setAttr(imgLink, "id", "limg_" + activeItem.hash);
					setAttr(eImage, "border", "0");
				} catch (e) {}
			}
			try {
				setAttr(imgLink, "_cke_saved_href", sLnkUrl);
				setAttr(imgLink, "target", elById("cmbLnkTarget").value);
			} catch (e) {}

			if (!linkPresent) {
				// append image to the link
				if (CKEDITOR.env.ie) {
					// clone the existing image object, otherwise IE behaves very very bad!
					eImage = eImage.clone(true, "ocmsimg");
				}
				eImage.appendTo(imgLink);
				eImage = imgLink;
			}
		}
	} // end simple image tag
	// insert the image element
	oEditor.insertElement(eImage);
}

/* Creates the enhanced image HTML if configured. */
function createEnhancedImage(eImage, eLink, eSpan) {
	if (isEnhancedPreview()) {
		// sub title and/or copyright information has to be inserted 
		var oNewElement = oEditor.document.createElement("span");
		if (CKEDITOR.env.ie && eLink == null) {
			// insert has to be done here (but only if no link was present) otherwise IE crashes
			oEditor.insertElement(oNewElement);
		}
		// now set the span attributes      
		var st = "width: " + elById("txtWidth").value + "px;";
		var al = elById("cmbAlign").value;
		if (al == "left" || al == "right") {
			st += " float: " + al + ";";
		}
		var imgVSp = elById("txtVSpace").value;
		var imgHSp = elById("txtHSpace").value;
		if (imgVSp != "" || imgHSp != "") {
			if (imgVSp == "") {
				imgVSp = "0";
			}
			if (imgHSp == "") {
				imgHSp = "0";
			}
			if (showEnhancedOptions && al != "") {
				var marginH = "right";
				if (al == "right") {
					marginH = "left";
				}
				st += "margin-bottom: " + imgVSp + "px; margin-" + marginH + ": " + imgHSp + "px;";
			} else {
				st += "margin: " + imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px";
			}
		}
		setAttr(oNewElement, "style", st);
		setAttr(oNewElement, "id", "aimg_" + activeItem.hash);

		// insert the image
		if (insertLinkToOriginal()) {
			var oLinkOrig = oEditor.document.createElement("a");
			if (useTbForLinkOriginal == true) {
				setAttr(oLinkOrig, "href", getLinkToOriginal());
				setAttr(oLinkOrig, "title", activeItem.title);
				setAttr(oLinkOrig, "class", "thickbox");
			} else {
				setAttr(oLinkOrig, "href", "#");
				setAttr(oLinkOrig, "onclick", getLinkToOriginal());
			}
			setAttr(oLinkOrig, "id", "limg_" + activeItem.hash);
			setAttr(eImage, "border", "0");
			oLinkOrig.append(eImage);
			oNewElement.append(oLinkOrig);
		} else {
			// simply add image
			oNewElement.append(eImage, false);
		}

		if (insertCopyright()) {
			// insert the 2nd span with the copyright information
			var copyText = elById("txtCopyright").value;
			if (copyText == "") {
				copyText = "&copy; " + activeItem.copyright;
			}
			var oSpan2 = oEditor.document.createElement("span");
			setAttr(oSpan2, "style", "display: block; clear: both;");
			setAttr(oSpan2, "class", "imgCopyright");
			setAttr(oSpan2, "id", "cimg_" + activeItem.hash);
			// do _not_ use appendText() otherwise IE ... crashes
			oSpan2.setHtml(copyText);
			oNewElement.append(oSpan2);
		}

		if (insertSubTitle()) {
			// insert the 3rd span with the subtitle
			var altText = elById("txtAlt").value;
			if (altText == "") {
				altText = activeItem.title;
			}
			var oSpan3 = oEditor.document.createElement("span");
			setAttr(oSpan3, "style", "display: block; clear: both;");
			setAttr(oSpan3, "class", "imgSubtitle");
			setAttr(oSpan3, "id", "simg_" + activeItem.hash);
			oSpan3.setHtml(altText);
			oNewElement.append(oSpan3, false);
			
		}
		// return the new object
		return oNewElement;
	} else {
		// return the original object
		return eImage;
	}
}

/* Creates the link to the original image. */
function getLinkToOriginal() {
	var linkUri = "";
	if (useTbForLinkOriginal == true) {
		linkUri += activeItem.linkpath;
	} else {
		linkUri += "javascript:window.open('";
		linkUri += vfsPopupUri;
		linkUri += "?uri=";
		linkUri += activeItem.linkpath;
		linkUri += "', 'original', 'width=";
		linkUri += activeItem.width;
		linkUri += ",height=";
		linkUri += activeItem.height;
		linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
	}
	return linkUri;
}

/* Updates the image element with the values of the input fields. */
function updateImage(e) {
	var txtUrl = activeItem.linkpath;
	var newWidth = activeItem.width;
	var newHeight = activeItem.height;
	if (initValues.scale == null || initValues.scale == "") {
		initValues.scale = "c:transparent,t:4,r=0,q=70";
	} else {
		if (initValues.scale.lastIndexOf(",") == initValues.scale.length - 1) {
			initValues.scale = initValues.scale.substring(0, initValues.scale.length - 1);
		}
	}

	if (activeItem.isCropped) {
		var newScale = "";
		if (initValues.scale != null && initValues.scale != "") {
			newScale += ",";
		}
		newScale += "cx:" + activeItem.cropx;
		newScale += ",cy:" + activeItem.cropy;
		newScale += ",cw:" + activeItem.cropw;
		newScale += ",ch:" + activeItem.croph;
		initValues.scale += newScale;
	} else if (getScaleValue(initValues.scale, "cx") != "") {
		initValues.scale = removeScaleValue(initValues.scale, "cx");
		initValues.scale = removeScaleValue(initValues.scale, "cy");
		initValues.scale = removeScaleValue(initValues.scale, "cw");
		initValues.scale = removeScaleValue(initValues.scale, "ch");
	}

	initValues.scale = removeScaleValue(initValues.scale, "w");
	initValues.scale = removeScaleValue(initValues.scale, "h");
	var newScale = "";
	var sizeChanged = false;
	if (initValues.scale != null && initValues.scale != "") {
		newScale += ",";
	}
	if (activeItem.newwidth > 0 && activeItem.width != activeItem.newwidth) {
		sizeChanged = true;
		newScale += "w:" + activeItem.newwidth;
		newWidth = activeItem.newwidth;
	}
	if (activeItem.newheight > 0 && activeItem.height != activeItem.newheight ) {
		if (sizeChanged == true) {
			newScale += ",";
		}
		sizeChanged = true;
		newScale += "h:" + activeItem.newheight;
		newHeight = activeItem.newheight;
	}
	initValues.scale += newScale;
	if (activeItem.isCropped || sizeChanged) {
		txtUrl += "?__scale=" + initValues.scale;
	}

	setAttr(e, "src", txtUrl);
	setAttr(e, "_cke_saved_src", txtUrl);
	setAttr(e, "alt"   , elById("txtAlt").value);
	setAttr(e, "width" , newWidth);
	setAttr(e, "height", newHeight);
	setAttr(e, "border", "");

	setAttr(e, "align" , elById("cmbAlign").value);

	var styleAttr = "";
	setAttr(e, "vspace", "");
	setAttr(e, "hspace", "");
	if (!isEnhancedPreview()) {
    		var imgAlign = elById("cmbAlign").value;
    		var vSp = elById("txtVSpace").value;
    		var hSp = elById("txtHSpace").value;
    		if (vSp == "") {
			vSp = "0";
		}
		if (hSp == "") {
			hSp = "0";
		}
    		if (showEnhancedOptions && imgAlign == "left") {
    			styleAttr = "margin-bottom: " + vSp + "px; margin-right: " + hSp + "px;";
    		} else if (showEnhancedOptions && imgAlign == "right") {
    			styleAttr = "margin-bottom: " + vSp + "px; margin-left: " + hSp + "px;";
    		} else {
			setAttr(e, "vspace", elById("txtVSpace").value);
			setAttr(e, "hspace", elById("txtHSpace").value);
		}
		if (insertLinkToOriginal()) {
			setAttr(e, "border", "0");
		}
	}

	// advanced attributes

	var idVal = elById("txtAttId").value;
	if (idVal == "" || idVal.substring(0, 5) == "iimg_") {
		idVal = "iimg_" + activeItem.hash;
	}
	setAttr(e, "id", idVal);

	setAttr(e, "dir", elById("cmbAttLangDir").value);
	setAttr(e, "lang", elById("txtAttLangCode").value);
	setAttr(e, "title", elById("txtAttTitle").value);
	setAttr(e, "class", elById("txtAttClasses").value);
	setAttr(e, "longDesc", elById("txtLongDesc").value);

	styleAttr += elById("txtAttStyle").value;
	setAttr(e, "style", styleAttr);
}

/* Returns the element with the given ID. */
function elById(elemId) {
	return document.getElementById(elemId);
}

/* Sets the attribute with the given value at the element, if value is empty, the attribute is removed. */
function setAttr(element, attName, attValue) {
	if ( attValue == null || attValue.length == 0 )
		element.removeAttribute(attName);
	else
		element.setAttribute( attName, attValue);
}

/* Returns the attribute value from of the element. */
function getAttr(element, attName, valueIfNull) {
	var oValue = element.getAttribute(attName);
	return (oValue == null ? valueIfNull : oValue);
}

function trimStr(val) {
	return val.replace(/(^\s*)|(\s*$)/g, "");
}

