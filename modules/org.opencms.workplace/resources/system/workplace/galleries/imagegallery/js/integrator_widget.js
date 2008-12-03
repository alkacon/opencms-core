<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

String params = request.getParameter(CmsImageGalleryExtended.PARAM_PARAMS);
if (CmsStringUtil.isNotEmpty(params)) {
	params = CmsStringUtil.substitute(params, "\r\n", "\\n");
        params = CmsStringUtil.substitute(params, "\n", "\\n");
}

%>
initValues = <% if (CmsStringUtil.isEmpty(params)) { out.print("{}"); } else { out.print(params); } %>;
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = false;

/* Initializes the image gallery popup window. */
function initPopup() {
	var sizeX = 650;
	var sizeY = 700;
	try {
		if (!isNaN(window.innerHeight) && window.innerHeight < (sizeY - 50)) {
			window.innerHeight = sizeY - 50;
		}
	} catch (e) {}
	var collectCategories = true;
	if (initValues.imagepath != null && initValues.imagepath != "") {
		$.post(vfsPathAjaxJsp, { action: "getactiveimage", imagepath: initValues.imagepath}, function(data){ loadActiveImage(data, true); });
	} else {
		$tabs.tabs("select", 1);
		$tabs.tabs("disable", 0);
	}
	window.resizeTo(sizeX, sizeY);
	if (initValues.startupfolder != null && initValues.startupfolder != "null") {
		initStartup();
	} else {
		setTimeout("getGalleries();", 50);
	}
	setTimeout("getCategories();", 100);
}

/* Initializes the category or gallery to show, if required. */
function initStartup() {
	if ((initValues.startuptype == "category" && categoriesLoaded == false)) {
		setTimeout("initStartup();", 100);
	} else {
		if (initValues.startuptype == "category") {
			getGalleries();
			for (var i = 0; i < categories.length; i++) {
				var currCat = categories[i];
				if (currCat.path == initValues.startupfolder) {
					selectCategory(currCat.path, i);
				}
			}
		} else {
			// start gallery folder specified, load gallery first
			$("#galleryfolders").hide();
			$("#galleryimages").show();
			$.post(vfsPathAjaxJsp, { action: "getgallery", gallerypath: initValues.startupfolder}, function(data){ initStartGallery(data); });
		}
	}
}

/* initializes start gallery, loads list of available galleries afterwards. */
function initStartGallery(data) {
	startGallery = eval("(" + data + ")");
	selectGallery(startGallery.path, -1);
	getGalleries(false);
}

/* OK Button was pressed, stores the image information back in the editor fields. */
function okPressed() {
	if (initValues.editedresource != null && initValues.editedresource != "") {
		var imgField = window.opener.document.getElementById("img." + initValues.fieldid);
		imgField.value = activeImage.sitepath;
		if (activeImage.isCropped) {
			var newScale = "";
			if (initValues.scale != null && initValues.scale != "") {
				newScale += ",";
			}
			newScale += "cx:" + activeImage.cropx;
			newScale += ",cy:" + activeImage.cropy;
			newScale += ",cw:" + activeImage.cropw;
			newScale += ",ch:" + activeImage.croph;
			initValues.scale += newScale;
		} else if (getScaleValue(initValues.scale, "cx") != "") {
			initValues.scale = removeScaleValue(initValues.scale, "cx");
			initValues.scale = removeScaleValue(initValues.scale, "cy");
			initValues.scale = removeScaleValue(initValues.scale, "cw");
			initValues.scale = removeScaleValue(initValues.scale, "ch");
		}
		if (initValues.useformats == true) {
			initValues.scale = removeScaleValue(initValues.scale, "w");
			initValues.scale = removeScaleValue(initValues.scale, "h");
			var formatBox = window.opener.document.getElementById("format." + initValues.fieldid);
			if (formatBox.selectedIndex != $("#formatselect").get(0).selectedIndex) {
				formatBox.selectedIndex = $("#formatselect").get(0).selectedIndex;
				window.opener.setImageFormat(initValues.fieldid, "imgFmts" + initValues.hashid);
			}
		} else {
			initValues.scale = removeScaleValue(initValues.scale, "w");
			initValues.scale = removeScaleValue(initValues.scale, "h");
			var newScale = "";
			var sizeChanged = false;
			if (initValues.scale != null && initValues.scale != "") {
				newScale += ",";
			}
			if (activeImage.newwidth > 0 && activeImage.width != activeImage.newwidth) {
				sizeChanged = true;
				newScale += "w:" + activeImage.newwidth;
			}
			if (activeImage.newheight > 0 && activeImage.height != activeImage.newheight ) {
				if (sizeChanged == true) {
					newScale += ",";
				}
				sizeChanged = true;
				newScale += "h:" + activeImage.newheight;
			}
			initValues.scale += newScale;
		}
		var scaleField = window.opener.document.getElementById("scale." + initValues.fieldid);
		scaleField.value = initValues.scale;
	}
	window.close();
}