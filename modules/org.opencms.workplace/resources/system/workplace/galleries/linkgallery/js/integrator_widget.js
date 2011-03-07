<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

String params = request.getParameter(A_CmsAjaxGallery.PARAM_PARAMS);
if (CmsStringUtil.isNotEmpty(params)) {
	params = CmsStringUtil.substitute(params, "\r\n", "\\n");
        params = CmsStringUtil.substitute(params, "\n", "\\n");
}

%>
initValues = <% if (CmsStringUtil.isEmpty(params)) { out.print("{}"); } else { out.print(params); } %>;
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
initValues.fieldid = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_FIELDID))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_FIELDID)); } %>";
initValues.viewonly = false;

itemsPerPage = 9;

/* Initializes the download gallery popup window. */
function initPopup() {

	var sizeX = 650;
	var sizeY = 700;
	if (window.locationbar && window.locationbar.visible == true) {
        	sizeY += 30;
    	} 
	window.resizeTo(sizeX, sizeY);
	
	var collectCategories = true;
	$("#categoryiteminfo > #dialogbuttons").show();
	$("#galleryiteminfo > #dialogbuttons").show();
	$("#galleryokbutton").hide();
	$("#categoryokbutton").hide();
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();


	
	var itemField = window.opener.document.getElementById(initValues.fieldid);
	if (itemField.value != null && itemField.value != "") {
		//path to selected item
		loadItemSitepath = new ItemSitepath();
		loadItemSitepath.path = new String(itemField.value);
	}
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
			// go to categories tab
			$tabs.tabs("select", 1);
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

/* OK Button was pressed, stores the item information back in the editor fields. */
function okPressed() {

	if ( initValues.fieldid != null && initValues.fieldid != "") {
		var linkField = window.opener.document.getElementById(initValues.fieldid);
		linkField.value = activeItem.sitepath;
		try {
			// toggle preview icon if possible
			window.opener.checkPreview(initValues.fieldid);
		} catch (e) {}
	}
	window.close();
}