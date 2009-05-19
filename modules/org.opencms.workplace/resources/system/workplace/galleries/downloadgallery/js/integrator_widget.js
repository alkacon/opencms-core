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

itemsPerPage = 7;

/* Initializes the download gallery popup window. */
function initPopup() {

	var sizeX = 650;
	var sizeY = 700;
	try {
		if (!isNaN(window.innerHeight) && window.innerHeight < (sizeY - 50)) {
			window.innerHeight = sizeY - 50;
		}
	} catch (e) {}
	var collectCategories = true;
	window.resizeTo(sizeX, sizeY);
	//$("#dialogbuttons").show();
	$("#categoryiteminfo > #dialogbuttons").show();
	$("#galleryiteminfo > #dialogbuttons").show();
	$("#galleryokbutton").hide();
	$("#categoryokbutton").hide();


	
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
		var imgField = window.opener.document.getElementById(initValues.fieldid);
		imgField.value = activeItem.sitepath;	
	}
	window.close();
}