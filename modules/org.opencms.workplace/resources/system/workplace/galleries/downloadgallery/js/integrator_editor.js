<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%
	
// get gallery instance
A_CmsAjaxGallery wp = new CmsAjaxDownloadGallery(pageContext, request, response); 

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
	// activate the "OK" button of the dialog
	window.parent.SetOkButton(true);
}

/* The OK button was hit, called by editor button click event. */
function Ok() {

	if (activeItem != null && activeItem != "") {
		link(activeItem.linkpath, activeItem.title, activeItem.description);
	}
	// close dialog
	return true;
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
	var sel = dialog.Selection.GetSelection();
	var text = "";
	if (oEditor.FCKSelection.GetSelection().createRange){
		text = oEditor.FCKSelection.GetSelection().createRange().text;
	} else {
		text = oEditor.FCKSelection.GetSelection();
	}
	
	if ((sel.GetType() == 'Text' || sel.GetType() == 'Control') && text != '') {
		return true;
	}
	return false; 
	
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
	linkInformation["style"] = "";
	linkInformation["class"] = "";
	linkInformation["title"] = title;
	createLink(linkInformation);
		
}	

function getSelectedLinkUri() {
	var loadItem = new ItemSitepath();
	var a = FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
        	FCK.Selection.SelectNode(a);
        	//path to resource
        	loadItem.path = a.getAttribute("_fcksavedurl");
        	// target attribute
		loadItem.target = a.getAttribute("target");
	}
	return loadItem;
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {

    var a = FCK.Selection.MoveToAncestorNode('A') ;
    if (a) {
    	// link present, manipulate it
        FCK.Selection.SelectNode(a);
        //a.href= linkInformation["href"];
	a = FCK.CreateLink(linkInformation["href"])[0];
		
    } else {
    	// new link, create it
        a = FCK.CreateLink(linkInformation["href"])[0];
        
    }
    
    if (linkInformation["target"] != "") {
		a.target = linkInformation["target"];
	} else {
		a.removeAttribute("target");
	}

    if (linkInformation["title"] != null && linkInformation["title"] != "") {
    		a.title = linkInformation["title"];
    } else {
		a.removeAttribute("title");
    }
	
	//if (USE_LINKSTYLEINPUTS) {
	//	if (linkInformation["class"] != "") {
	//		a.setAttribute("class", linkInformation["class"]);
	//	} else {
	//		a.removeAttribute("class");
	//	}
	//	if (linkInformation["style"] != "") {
	//		a.style.cssText = linkInformation["style"];
	//	} else {
	//		a.removeAttribute("style");
	//	}

	//}
} 


// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
	FCK.InsertHtml(htmlContent);
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