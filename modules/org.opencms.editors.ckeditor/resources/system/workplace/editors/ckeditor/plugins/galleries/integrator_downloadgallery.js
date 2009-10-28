<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%
	
// get gallery instance
A_CmsAjaxGallery wp = new CmsAjaxDownloadGallery(pageContext, request, response); 

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

	//set up buttons
	$("#gallerycancelbutton").remove();
	$("#categorycancelbutton").remove();
	$("#galleryokbutton").remove();
	$("#categoryokbutton").remove();
	$("#galleryresetsearchbutton").hide();
	$("#categoryresetsearchbutton").hide();
	
	if (hasSelectedText() == true) {
		loadItemSitepath = getSelectedLinkUri();
	}
	setTimeout("getGalleries();", 50);
	setTimeout("getCategories();", 100);
}

/* Do additional stuff when active item is loaded. */
function activeItemAdditionalActions() {
	// nothing to do here
}

/* The OK button was hit, called by editor button click event. */
function Ok() {

	if (activeItem != null && activeItem != "") {
		link(activeItem.linkpath, activeItem.title, activeItem.description);
	}
}

function link(uri, title, desc) {
		
	if (hasSelectedText() == true) {
		// text selected.
		setLink(uri, title);	
	} else {
		pasteLink(uri, title, desc);
	}
		
}

/* Pastes a link to the current position of the editor */
function pasteLink(uri, title, desc) {
	
	var result = "<a href=\"";
	result += uri;
	result += "\" title=\"";
	result += escapeBrackets(desc);
	result += "\" target=\"";
	if (modeType == "gallery") {
		result += $("#gallerylinktarget").val();
	} else {
		result += $("#categorylinktarget").val();
	}
	result += "\">";
	result += escapeBrackets(title);
	result += "<\/a>";
	insertHtml(result);
}

// checks if a text part has been selected by the user
function hasSelectedText() {
	var sel = oEditor.getSelection();
	var ranges = sel.getRanges();
	if (ranges.length == 1 && ranges[0].collapsed) {
		return false;
	}
	return true;
	
}

function setLink(uri, title) {
	
	var linkInformation = new Object();
	linkInformation["type"] = "link";
	linkInformation["href"] = uri;
	if (modeType == "gallery") {
		linkInformation["target"] = $("#gallerylinktarget").val();
	} else {
		linkInformation["target"] = $("#categorylinktarget").val();
	}
	linkInformation["title"] = title;
	createLink(linkInformation);
		
}

// returns the selected <a> element or null, if no link is selected
function getSelectedLinkElement() {
	var element = oEditor.getSelection().getSelectedElement();
	if (!element) {
		// failed, try to get the start element of the selection
		element = oEditor.getSelection().getStartElement();
	}
	if (element && element.getName() != "a") {
	    	// selected element is no link, try to get the surrounding anchor
	    	element = element.getAscendant("a", false);
  	}
  	return element;
}

function getSelectedLinkUri() {
	var loadItem = new ItemSitepath();
	var a = getSelectedLinkElement();
    	if (a) {
    		// link present, get path to resource
        	loadItem.path = a.getAttribute("_cke_saved_href");
        	// target attribute
		loadItem.target = a.getAttribute("target");
	}
	return loadItem;
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {

	var attributes = {href: linkInformation["href"]};
	var removeAttributes = [];
	
	// set the hidden editor specific attribute, otherwise nothing works
	attributes._cke_saved_href = linkInformation["href"];
	if (linkInformation["target"] != null && linkInformation["target"] != "") {
		attributes.target = linkInformation["target"];
	} else {
		removeAttributes.push("target");
	}
	if (linkInformation["title"] != null && linkInformation["title"] != "") {
		attributes.title = linkInformation["title"];
	} else {
		removeAttributes.push("title");
	}

	// get the element of the current selection
	var element = getSelectedLinkElement();

	if (!element) {
		// no link element found, create new one
		var style = new CKEDITOR.style({ element : "a", attributes : attributes });
		style.type = CKEDITOR.STYLE_INLINE;
		style.apply(oEditor.document); 
	} else {
		// link element exists, set attributes
		element.setAttributes(attributes);
		element.removeAttributes(removeAttributes);
	    	delete element;
	}

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