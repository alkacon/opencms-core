<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

String params = request.getParameter(A_CmsAjaxGallery.PARAM_PARAMS);
if (CmsStringUtil.isNotEmpty(params)) {
	params = CmsStringUtil.substitute(params, "\r\n", "\\n");
        params = CmsStringUtil.substitute(params, "\n", "\\n");
}

%>
initValues = <% if (CmsStringUtil.isEmpty(params)) { out.print("{}"); } else { out.print(params); } %>;
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = false;

/* Initializes the item gallery popup window. bin hier ;-)*/
function initPopup() {
	var sizeX = 650;
	var sizeY = 710;
	if (navigator.userAgent.indexOf("MSIE") != -1) {
		sizeY = 720;
	}
	if (window.locationbar && window.locationbar.visible == true) {
        	sizeY += 30;
    	} 
	window.resizeTo(sizeX, sizeY);
	$("#dialogbuttons").hide();
	$("#closebutton").show();
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();
	
	// select the gallery tab
	$tabs.tabs("select", 1);
	$tabs.tabs("disable", 0);

	if (initValues.startupfolder != null) {
		// start gallery folder specified, load gallery first
		$("#galleryfolders").hide();  //hide
		$("#galleryitems").show();   //show
		$.post(vfsPathAjaxJsp, { action: "getgallery", gallerypath: initValues.startupfolder}, function(data){ initStartGallery(data); });
	}

}

/* initializes start gallery, loads list of available galleries afterwards. */
function initStartGallery(data) {
	startGallery = eval("(" + data + ")");
	selectGallery(startGallery.path, -1);
	getGalleries(false);
}