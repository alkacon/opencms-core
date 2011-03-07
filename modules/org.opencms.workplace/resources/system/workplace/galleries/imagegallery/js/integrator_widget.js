<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

String params = request.getParameter(A_CmsAjaxGallery.PARAM_PARAMS);
if (CmsStringUtil.isNotEmpty(params)) {
	params = CmsStringUtil.substitute(params, "\r\n", "\\n");
        params = CmsStringUtil.substitute(params, "\n", "\\n");
}

%>
//params for widget mode are initialized and added to request parameters in getSkinUri()+ "/components/widgets/vfsimage.js
initValues = <% if (CmsStringUtil.isEmpty(params)) { out.print("{}"); } else { out.print(params); } %>;
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
// simple widget flag
initValues.widgetmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(CmsAjaxImageGallery.PARAM_WIDGETMODE))) { out.print(""); } else { out.print(request.getParameter(CmsAjaxImageGallery.PARAM_WIDGETMODE)); } %>";
initValues.viewonly = false;

/* Initializes the image gallery popup window. */
function initPopup() {
	var sizeX = 650;
	var sizeY = 700;
	if (window.locationbar && window.locationbar.visible == true) {
        	sizeY += 30;
    	} 
	window.resizeTo(sizeX, sizeY);
	var collectCategories = true;

	if (initValues.imagepath != null && initValues.imagepath != "") {
		var path = initValues.imagepath; 
		if (initValues.widgetmode == "simple") {
		 	path = removeParamFromPath(initValues.imagepath);
		 }
		$.post(vfsPathAjaxJsp, { action: "getactiveitem", itempath: path}, function(data){ loadActiveItem(data, true); });
	} else {
		$tabs.tabs("select", 1);
		$tabs.tabs("disable", 0);
	}
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();
	if (initValues.startupfolder != null && initValues.startupfolder != "null" && initValues.startupfolder != "") {
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
			// if gallery tab is selected, swich to category tab
			var selectedTab = $tabs.tabs('option', 'selected');
			if (selectedTab == 1) {
				$tabs.tabs("select", 2);
			}
			getGalleries();
			for (var i = 0; i < categories.length; i++) {
				var currCat = categories[i];
				if (currCat.path == initValues.startupfolder) {
					selectCategory(currCat.path, i);
				}
			}
			$("#galleryfolders").hide();
			$("#galleryitems").show();
		} else {
			// start gallery folder specified, load gallery first
			$("#galleryfolders").hide();
			$("#galleryitems").show();
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
	if (initValues.widgetmode == "simple") {
		// simple image gallery widget
		if ( initValues.fieldid != null && initValues.fieldid != "") {
			var imgField = window.opener.document.getElementById(initValues.fieldid);
			var imagePath = activeItem.sitepath;
			if (activeItem.isCropped) {
				var newScale = "";
				if (initValues.scale != null && initValues.scale != "")  {
					newScale += ",";
				}
				newScale += "cx:" + activeItem.cropx;
				newScale += ",cy:" + activeItem.cropy;
				newScale += ",cw:" + activeItem.cropw;
				newScale += ",ch:" + activeItem.croph;

				initValues.scale += newScale;
								
			} 
			// remove cropping parameter
			else if (getScaleValue(initValues.scale, "cx") != "") {
				initValues.scale = removeScaleValue(initValues.scale, "cx");
				initValues.scale = removeScaleValue(initValues.scale, "cy");
				initValues.scale = removeScaleValue(initValues.scale, "cw");
				initValues.scale = removeScaleValue(initValues.scale, "ch");

			}
			initValues.scale = removeScaleValue(initValues.scale, "w");
			initValues.scale = removeScaleValue(initValues.scale, "h");
			
			var newScale = "";
			var sizeChanged = false;
			// comma to separate the content
			if (initValues.scale != null && initValues.scale != "") {
				newScale += ",";
			}
			if (activeItem.newwidth > 0 && activeItem.width != activeItem.newwidth) {
				sizeChanged = true;
				newScale += "w:" + activeItem.newwidth;
			}
			if (activeItem.newheight > 0 && activeItem.height != activeItem.newheight ) {
				if (sizeChanged == true) {
					newScale += ",";
				}
				sizeChanged = true;
				newScale += "h:" + activeItem.newheight;
			}
			if (newScale.length > 1) {
				initValues.scale += newScale;
			}

			if (initValues.scale != null && initValues.scale != "") {
				imagePath += "?__scale=";
				imagePath += initValues.scale;
			}
				
			// write the path with request parameters to the input field
			imgField.value = imagePath;

		}
	} else {
		// widget mode: VFS image widget
		if (initValues.editedresource != null && initValues.editedresource != "") {
			var imgField = window.opener.document.getElementById("img." + initValues.fieldid);
			imgField.value = activeItem.sitepath;
			if (activeItem.isCropped) {
				var newScale = "";
				if (initValues.scale != null && initValues.scale != "" && initValues.scale.charAt(initValues.scale.length - 1) != ",") {
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
			if (initValues.useformats == true) {
				var formatBox = window.opener.document.getElementById("format." + initValues.fieldid);
				if (formatBox.selectedIndex != $("#formatselect").get(0).selectedIndex) {
					formatBox.selectedIndex = $("#formatselect").get(0).selectedIndex;
					window.opener.setImageFormat(initValues.fieldid, "imgFmts" + initValues.hashid);
				}
			}
			initValues.scale = removeScaleValue(initValues.scale, "w");
			initValues.scale = removeScaleValue(initValues.scale, "h");
			if (initValues.useformats != true || activeItem.isCropped) {
				var newScale = "";
				var sizeChanged = false;
				if (initValues.scale != null && initValues.scale != ""  && initValues.scale.charAt(initValues.scale.length - 1) != ",") {
					newScale += ",";
				}
				if (activeItem.newwidth > 0 && activeItem.width != activeItem.newwidth) {
					sizeChanged = true;
					newScale += "w:" + activeItem.newwidth;
				}
				if (activeItem.newheight > 0 && activeItem.height != activeItem.newheight ) {
					if (sizeChanged == true) {
						newScale += ",";
					}
					sizeChanged = true;
					newScale += "h:" + activeItem.newheight;
				}
				initValues.scale += newScale;
			}
			
			var scaleField = window.opener.document.getElementById("scale." + initValues.fieldid);
			scaleField.value = initValues.scale;
			var ratioField = window.opener.document.getElementById("imgrat." + initValues.fieldid);
			ratioField.value = activeItem.width / activeItem.height;
		}
	}
	try {
		// toggle preview icon if possible
		if (initValues.widgetmode == "simple") {
			window.opener.checkPreview(initValues.fieldid);
		} else {
			window.opener.checkVfsImagePreview(initValues.fieldid);
		}
	} catch (e) {}
	window.close();
}

/* removes scale parameter from the image path if available. */
function removeParamFromPath(pathWithParam) {
	var path = "";
	var index = pathWithParam.indexOf( "?__scale=" );
	if (index == -1) {
		path = pathWithParam;
	} else {
		path = pathWithParam.substring(0, eval(index) );
	}
	return path;
}