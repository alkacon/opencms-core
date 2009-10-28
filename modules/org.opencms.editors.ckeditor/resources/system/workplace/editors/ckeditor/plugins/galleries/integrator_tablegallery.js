<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%
	
// get gallery instance
A_CmsAjaxGallery wp = new CmsAjaxTableGallery(pageContext, request, response); 

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
	Ok();
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

/* Size of the preview area. */
previewX = 600;
previewY = 230;

itemsPerPage = 7;

/* Initialize the dialog values. */
initValues = {};
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = false;
initValues.editedresource = "<%= editedResource %>";
//saves the type of the gallery: 'gallery' or 'category'. It is used to choose the appropriate select box in widget mode.
var modeType = "";

/* Initializes the download gallery popup window. */
function initPopup() {
	$("#dialogbuttons").remove();
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();
	
	//always open the gallery tab
	$tabs.tabs("select", 1);
	$tabs.tabs("disable", 0);
	
	setTimeout("getGalleries();", 50);
	setTimeout("getCategories();", 100);
}

/* Do additional stuff when active item is loaded. */
function activeItemAdditionalActions() {
	// activate the "OK" button of the dialog
	//window.parent.SetOkButton(true);
}

/* The OK button was hit, called by editor button click event. */
function Ok() {
	if (activeItem != null && activeItem != "") {
		var htmlContent = activeItem.htmltable;
		// convert to string before inserting 
		insertHtml(htmlContent.toString());
	}
	// close dialog
	return true;
}

// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
	oEditor.insertHtml(htmlContent);
}

function escapeBrackets(s) {
	var searchResultStart = s.search(/\[.+/);
	var searchResultEnd = s.search(/.+\]/);
	var cut = (searchResultStart == 0 && searchResultEnd != -1 && s.charAt(s.length - 1) == ']');
	if (cut) {
		// cut off the first '['
		s = s.substring(1,s.length);
		// cut off the last ']'
		s = s.substring(0,s.length-1);
	}

	return s;
}