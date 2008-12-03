<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

String params = request.getParameter(CmsImageGalleryExtended.PARAM_PARAMS);
if (CmsStringUtil.isNotEmpty(params)) {
	params = CmsStringUtil.substitute(params, "\r\n", "\\n");
        params = CmsStringUtil.substitute(params, "\n", "\\n");
}

%>
initValues = <% if (CmsStringUtil.isEmpty(params)) { out.print("{}"); } else { out.print(params); } %>;
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = true;

/* Initializes the image gallery popup window. */
function initPopup() {
	var sizeX = 650;
	var sizeY = 750;
	window.resizeTo(sizeX, sizeY);
	try {
		if (!isNaN(window.innerHeight) && window.innerHeight < (sizeY - 50)) {
			window.innerHeight = sizeY - 50;
		}
	} catch (e) {}
	$("#closebutton").show();

	if (initValues.startupfolder != null) {
		// start gallery folder specified, load gallery first
		$("#galleryfolders").hide();
		$("#galleryimages").show();
		$.post(vfsPathAjaxJsp, { action: "getgallery", gallerypath: initValues.startupfolder}, function(data){ initStartGallery(data); });
	}
}

/* initializes start gallery, loads list of available galleries afterwards. */
function initStartGallery(data) {
	startGallery = eval("(" + data + ")");
	selectGallery(startGallery.path, -1);
	getGalleries(false);
}