<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

String editedResource = "";
if (CmsStringUtil.isNotEmpty(wp.getParamResource())) {
	editedResource = wp.getParamResource();
}

%><%= wp.getJsp().getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>
/* Initialize important FCKeditor variables from editor. */
var dialog		= window.parent;
var oEditor		= dialog.InnerDialogLoaded();
var FCK			= oEditor.FCK;
var FCKConfig		= oEditor.FCKConfig;
var FCKBrowserInfo	= oEditor.FCKBrowserInfo;

/* Enables or disables the enhanced image dialog options. */
var showEnhancedOptions = FCKConfig.ShowEnhancedOptions;
var useTbForLinkOriginal = FCKConfig.UseTbForLinkOriginal;

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
		GetE("txtAlt").value = imgTitle;
	}
	// activate the "OK" button of the dialog
	window.parent.SetOkButton(true);
}

/* Opens the file browser popup for the link dialog. */
function LnkBrowseServer() {
        OpenFileBrowser(FCKConfig.LinkBrowserURL, FCKConfig.LinkBrowserWindowWidth, FCKConfig.LinkBrowserWindowHeight);
}

/* Triggered by the file browser popup to set the selected URL in the input field. */
function SetUrl( url, width, height, alt ) {
        GetE("txtLnkUrl").value = url;
}

/* Loads the selected image from the editor, if available. */
function loadSelection() {
	// get the selected image
	oImage = dialog.Selection.GetSelectedElement();
	if (oImage && oImage.tagName != "IMG" && oImage.tagName != "SPAN" && !(oImage.tagName == "INPUT" && oImage.type == "image")) {
		oImage = null;
	}
	// get the active link
	oLink = dialog.Selection.GetSelection().MoveToAncestorNode("A");

	if (!oImage) {
		// no image selected, nothing to do...
		GetE('cmbAlign').value = "left";
		GetE("txtHSpace").value = "5";
		GetE("txtVSpace").value = "5";
		GetE("imageBorder").checked = true;
		return;
	}

	var altText = "";
	var copyText = "";
	var imgBorder =	false;
	var imgHSp = "";
	var imgVSp = "";
	var imgAlign = GetAttribute(oImage, "align", "");;
	if (dialog.Selection.GetSelection().HasAncestorNode("SPAN") || dialog.Selection.GetSelection().HasAncestorNode("TABLE")) {
		if (FCK.Selection.HasAncestorNode("SPAN")) {
			oSpan =	dialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
		} else {
			oSpan =	dialog.Selection.GetSelection().MoveToAncestorNode("TABLE");
		}
		try {
			var idPart = oSpan.getAttribute("id").substring(1);
			if (idPart == oImage.getAttribute("id").substring(1)) {

				var altElem = oEditor.FCK.EditorDocument.getElementById("s" + idPart);
				if (altElem) {
					altText	= altElem.firstChild.data;
					GetE("insertAlt").checked = true;
				}

				var cpElem = oEditor.FCK.EditorDocument.getElementById("c" + idPart);
				if (cpElem) {
					copyText = cpElem.firstChild.data;
					GetE("insertCopyright").checked = true;
				}
				var divElem = oEditor.FCK.EditorDocument.getElementById("a" + idPart);
				imgHSp = divElem.style.marginLeft;
				if (imgAlign == "left") {
			 		imgHSp = divElem.style.marginRight;
			 	} else if (imgAlign == "right") {
			 		imgHSp = divElem.style.marginLeft;
			 	}
				imgVSp = divElem.style.marginBottom;
			}
		} catch	(e) {}
	 } else	{
	 	if (imgAlign == "left") {
	 		imgHSp = oImage.style.marginRight;
			imgVSp = oImage.style.marginBottom;
			if (imgHSp == "") {
				imgHSp = GetAttribute(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = GetAttribute(oImage, "vspace", "");
			}
	 	} else if (imgAlign == "right") {
	 		imgHSp = oImage.style.marginLeft;
	 		imgVSp = oImage.style.marginBottom;
	 		if (imgHSp == "") {
				imgHSp = GetAttribute(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = GetAttribute(oImage, "vspace", "");
			}
	 	} else {
			imgHSp = GetAttribute(oImage, "hspace", "");
			imgVSp = GetAttribute(oImage, "vspace", "");
		}
	}
	var cssTxt = oImage.style.cssText;
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
		altText	= GetAttribute(oImage,	"alt", "");
	}

	var sUrl = oImage.getAttribute("_fcksavedurl");
	if (sUrl == null) {
		sUrl = GetAttribute(oImage, "src", "");
	}
	var paramIndex = sUrl.indexOf("?__scale");
	if (paramIndex != -1) {
		initValues.scale = sUrl.substring(paramIndex + 9);
		sUrl = sUrl.substring(0, paramIndex);
	}

	initValues.itempath = sUrl;

	GetE("txtAlt").value = altText;
	if (copyText !=	"")	{
		GetE("txtCopyright").value = copyText;
	}
	
	if (isNaN(imgHSp) && imgHSp.indexOf("px") != -1)	{	
		imgHSp = imgHSp.substring(0, imgHSp.length - 2);
	}
	if (isNaN(imgVSp) && imgVSp.indexOf("px") != -1)	{	
		imgVSp = imgVSp.substring(0, imgVSp.length - 2);
	}

	if (imgHSp != "" || imgVSp != "") {
		imgBorder = true;
	}
	

	if (imgBorder) {
		GetE("txtVSpace").value	= imgVSp;
		GetE("txtHSpace").value	= imgHSp;
		GetE("imageBorder").checked = true;
	}

	GetE("cmbAlign").value = imgAlign;

	var iWidth, iHeight;

	var regexSize = /^\s*(\d+)px\s*$/i ;

	if (oImage.style.width)	{
		var aMatch = oImage.style.width.match(regexSize);
		if (aMatch) {
			iWidth = aMatch[1];
			oImage.style.width = "";
		}
	}

	if (oImage.style.height) {
		var aMatch = oImage.style.height.match(regexSize);
		if (aMatch) {
			iHeight = aMatch[1];
			oImage.style.height = "";
		}
	}

	iWidth = iWidth ? iWidth : GetAttribute(oImage, "width", "");
	iHeight = iHeight ? iHeight : GetAttribute(oImage, "height", "");

	initValues.imgwidth = "" + iWidth;
	initValues.imgheight = "" + iHeight;

	// get Advanced	Attributes
	GetE("txtAttId").value = oImage.id;
	GetE("cmbAttLangDir").value = oImage.dir;
	GetE("txtAttLangCode").value = oImage.lang;
	GetE("txtAttTitle").value = oImage.title;
	GetE("txtAttClasses").value = oImage.getAttribute("class", 2) || "";
	GetE("txtLongDesc").value = oImage.longDesc;
	GetE("txtAttStyle").value = cssTxt;

	if (oLink) {
		var lnkUrl = oLink.getAttribute("_fcksavedurl");
		if (lnkUrl == null) {
			lnkUrl = oLink.getAttribute("href", 2);
		}
		if (lnkUrl != sUrl) {
			GetE("txtLnkUrl").value = lnkUrl;
			GetE("cmbLnkTarget").value = oLink.target;
		}
		var idAttr = oLink.id;
		if (idAttr != null && idAttr.indexOf("limg_") == 0) {
			GetE("linkOriginal").checked = true;
		}
	}
}

/* Resets the image alternative text to the original value. */
function resetAltText() {
	var imgTitle = activeItem.title;
	if (activeItem.description != "") {
		imgTitle = activeItem.description;
	}
	GetE("txtAlt").value = imgTitle;
}

/* Resets the image copyright text to the original value. */
function resetCopyrightText() {
	var copyText = activeItem.copyright;
	if (copyText == null || copyText == "") {
		copyText = "";
	} else {
		copyText = "&copy; " + copyText;
	}
	GetE("txtCopyright").value = copyText;
}

/* Toggles the image spacing values. */
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
	var elem = GetE(elemName);
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
function Ok() {
	var bHasImage = oImage != null;
	var imgCreated = false;

	if (!bHasImage) {
		oImage = FCK.InsertElement("img");
		// set flag that image is newly created
		imgCreated = true;
	}  else {
		oEditor.FCKUndo.SaveUndoStep();
	}

	updateImage(oImage);

	// now its getting difficult, be careful when modifying anything below this comment...

	if (isEnhancedPreview() && oLink) {
		// original link has to be removed if a span is created in enhanced options
		FCK.Selection.SelectNode(oLink);
		FCK.ExecuteNamedCommand("Unlink");
	}

	// now we set the image object either to the image or in case of enhanced options to a span element
	oImage = createEnhancedImage();

	if (showEnhancedOptions && oSpan != null && (oSpan.id.substring(0, 5) == "aimg_" || oSpan.id.substring(0, 5) == "timg_")) {
		// span is already present, select it
		FCK.Selection.SelectNode(oSpan);
		// remove child elements of span
		while (oSpan.firstChild != null) {
			oSpan.removeChild(oSpan.firstChild);
		}
	}

	if (!imgCreated) {
		// delete the selection (either the image or the complete span) if the image was not freshly created
		FCK.Selection.Delete();
		// now insert the new element
		oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
	} else {
		// this handles the initial creation of an image, might be buggy...
		if (!oEditor.FCKBrowserInfo.IsIE) {
			// we have to differ here, otherwise the stupid IE creates the image twice!
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		} else if (isEnhancedPreview()) {
			// in IE... insert the new element to make sure the span is inserted
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		}
	}

	if (oImage.tagName != "SPAN") {
		// the object to insert is a simple image, check the link to set
		FCK.Selection.SelectNode(oImage);

		oLink = FCK.Selection.MoveToAncestorNode("A");

		var sLnkUrl = GetE("txtLnkUrl").value.Trim();
		var linkOri = "";

		if (insertLinkToOriginal()) {
			sLnkUrl = "#";
			linkOri = getLinkToOriginal();
		} else if (sLnkUrl == "#") {
			sLnkUrl = "";
		}

		if (sLnkUrl.length == 0) {
			if (oLink) {
				oLink.removeAttribute("class");
				FCK.ExecuteNamedCommand("Unlink");
			}
		} else {
			if (oLink) {  
				// remove an existing link and create it newly, because otherwise the "onclick" attribute does not vanish in Mozilla
				oLink.removeAttribute("class");
				FCK.ExecuteNamedCommand("Unlink");
				oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];
			} else {
				// creating a new link
				if (!bHasImage) {
					oEditor.FCKSelection.SelectNode(oImage);
				}

				oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];

				if (!bHasImage)	{
					oEditor.FCKSelection.SelectNode(oLink);
					oEditor.FCKSelection.Collapse(false);
				}
			}

			if (linkOri != "") {
				// set the necessary attributes for the link to original image
				try {
					if (useTbForLinkOriginal == true) {
						oLink.setAttribute("href", linkOri);
						oLink.setAttribute("title", GetE("txtAlt").value);
						oLink.setAttribute("class", "thickbox");
						sLnkUrl = linkOri;
					} else {
						oLink.setAttribute("onclick", linkOri);
					}
					oLink.setAttribute("id", "limg_" + activeItem.hash);
					oImage.setAttribute("border", "0");
				} catch (e) {}
			}
			try {
				SetAttribute(oLink, "_fcksavedurl", sLnkUrl);
				SetAttribute(oLink, "target", GetE("cmbLnkTarget").value);
			} catch (e) {}
		}
	} // end simple image tag
	return true;
}

/* Creates the enhanced image HTML if configured. */
function createEnhancedImage() {
	if (isEnhancedPreview()) {
		// sub title and/or copyright information has to be inserted 
		var oNewElement = oEditor.FCK.EditorDocument.createElement("SPAN");
		// now set the span attributes      
		var st = "width: " + GetE("txtWidth").value + "px;";
		var al = GetE("cmbAlign").value;
		if (al == "left" || al == "right") {
			st += " float: " + al + ";";
		}
		var imgVSp = GetE('txtVSpace').value;
		var imgHSp = GetE('txtHSpace').value;
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
		oNewElement.style.cssText = st;
		SetAttribute(oNewElement, "id", "aimg_" + activeItem.hash);

		// insert the image
		if (insertLinkToOriginal()) {
			var oLinkOrig = oEditor.FCK.EditorDocument.createElement("A");
			if (useTbForLinkOriginal == true) {
				oLinkOrig.href = getLinkToOriginal();
				oLinkOrig.setAttribute("title", activeItem.title);
				oLinkOrig.setAttribute("class", "thickbox");
			} else {
				oLinkOrig.href = "#";
				oLinkOrig.setAttribute("onclick", getLinkToOriginal());
			}
			oLinkOrig.setAttribute("id", "limg_" + activeItem.hash);
			oImage.setAttribute("border", "0");
			oLinkOrig.appendChild(oImage);
			oNewElement.appendChild(oLinkOrig);
		} else {
			// simply add image
			oNewElement.appendChild(oImage);
		}

		if (insertCopyright()) {
			// insert the 2nd span with the copyright information
			var copyText = GetE("txtCopyright").value;
			if (copyText == "") {
				copyText = "&copy; " + activeItem.copyright;
			}
			var oSpan2 = oEditor.FCK.EditorDocument.createElement("SPAN");
			oSpan2.style.cssText = "display: block; clear: both;";
			oSpan2.className = "imgCopyright";
			oSpan2.id = "cimg_" + activeItem.hash;
			oSpan2.innerHTML = copyText;
			oNewElement.appendChild(oSpan2);
		}

		if (insertSubTitle()) {
			// insert the 3rd span with the subtitle
			var altText = GetE("txtAlt").value;
			if (altText == "") {
				altText = activeItem.title;
			}
			var oSpan3 = oEditor.FCK.EditorDocument.createElement("SPAN");
			oSpan3.style.cssText = "display: block; clear: both;";
			oSpan3.className = "imgSubtitle";
			oSpan3.id = "simg_" + activeItem.hash;
			oSpan3.innerHTML = altText;
			oNewElement.appendChild(oSpan3);
		}

		// return the new object
		return oNewElement;
	} else {
		// return the original object
		return oImage;
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

	e.src = txtUrl;
	SetAttribute(e, "_fcksavedurl", txtUrl);
	SetAttribute(e, "alt"   , GetE("txtAlt").value);
	SetAttribute(e, "width" , newWidth);
	SetAttribute(e, "height", newHeight);
	SetAttribute(e, "border", "");

	SetAttribute(e, "align" , GetE("cmbAlign").value);

	var styleAttr = "";
	SetAttribute(e, "vspace", "");
	SetAttribute(e, "hspace", "");
	if (!isEnhancedPreview()) {
    		var imgAlign = GetE("cmbAlign").value;
    		var vSp = GetE("txtVSpace").value;
    		var hSp = GetE("txtHSpace").value;
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
			SetAttribute(e, "vspace", GetE("txtVSpace").value);
			SetAttribute(e, "hspace", GetE("txtHSpace").value);
		}
		if (insertLinkToOriginal()) {
			SetAttribute(e, "border", "0");
		}
	}

	// advanced attributes

	var idVal = GetE("txtAttId").value;
	if (idVal == "" || idVal.substring(0, 5) == "iimg_") {
		idVal = "iimg_" + activeItem.hash;
	}
	SetAttribute(e, "id", idVal);

	SetAttribute(e, "dir", GetE("cmbAttLangDir").value);
	SetAttribute(e, "lang", GetE("txtAttLangCode").value);
	SetAttribute(e, "title", GetE("txtAttTitle").value);
	SetAttribute(e, "class", GetE("txtAttClasses").value);
	SetAttribute(e, "longDesc", GetE("txtLongDesc").value);

	styleAttr += GetE("txtAttStyle").value;
	if (oEditor.FCKBrowserInfo.IsIE) {
		e.style.cssText = styleAttr;
	} else {
		SetAttribute(e, "style", styleAttr);
	}
}