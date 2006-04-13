<%@ page import="
	org.opencms.jsp.*,
	org.opencms.workplace.editors.*, 
	org.opencms.editors.fckeditor.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsFCKEditor wp = new CmsFCKEditor(cms);

if (wp.isHelpEnabled()) {
	%>
function openOnlineHelp(wpUri) {
	if (wpUri == null || wpUri == "") {
		if (top.body.top.body.admin_content != null && top.body.top.body.admin_content.onlineHelpUriCustom != null) {
			wpUri = top.body.top.body.admin_content.onlineHelpUriCustom;
		}
		else{
			if (top.body != null && top.body.explorer_body != null) {
				// determine currently shown explorer item
				try {
					wpUri = top.body.explorer_body.explorer_files.location.pathname;
				} catch (e) {}
			} else if (top.body != null && top.body.admin_content != null) {
				// determine currently shown administration item
				var parameters = "";
				try {
					parameters = decodeURIComponent(top.body.admin_content.tool_title.location.search);
				} catch (e) {
					try {
						parameters = decodeURIComponent(top.body.admin_content.location.search);
					} catch (e) {}
				}
				var pathIndex = parameters.lastIndexOf("path=");
				if (pathIndex != -1) {
					parameters = parameters.substring(pathIndex + 5);
					if (parameters.indexOf("&") != -1) {
						parameters = parameters.substring(0, parameters.indexOf("&"));
					}
					wpUri = parameters + "/";
				} else {
					wpUri = "/administration/"
				}
			} else if(top.body != null) {
				wpUri = top.body.location.pathname;
			}
		}
	}
	if (wpUri==null) {
		wpUri="/system/workplace/";
	}
	window.open("<%= cms.link("/system/workplace/locales/" + wp.getLocale() + "/help/index.html") %>?buildframe=true&workplaceresource=" + wpUri, "cmsonlinehelp", "toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width=700,height=450");
}
	<%
}

%>
var workplacePath="<%= cms.link("/system/workplace/") %>";
var USE_LINKSTYLEINPUTS = false;

function execAction(form, action, target) {
	form.content.value = encodeURIComponent(FCK.GetXHTML(false));
	form.action.value = action;
	form.target = target;
	form.submit(); 
}

// opens the specified gallery in a popup window
function openGallery(galleryType) {
	openWindow = window.open(workplacePath + "galleries/gallery_fs.jsp?gallerytypename=" + galleryType, "GalleryBrowser", "width=650, height=700, resizable=yes, top=20, left=100");
	focusCount = 1;
	openWindow.focus();
}

// opens the link dialog window
function openLinkDialog(errorMessage) {
	openAnchorDialogWindow("link", errorMessage);
}

// opens the anchor dialog window
function openAnchorDialog(errorMessage) {
	openAnchorDialogWindow("anchor", errorMessage);
}

// opens the anchor or link dialog window depending on the given link type ("link" or "anchor")
function openAnchorDialogWindow(linkType, errorMessage) { 
	if (hasSelectedText()) {
		var winheight;
		var winwidth;
		if (linkType == "link") {
			winheight = (USE_LINKSTYLEINPUTS?220:170);
			winwidth = 480;
		} else {
			winheight = (USE_LINKSTYLEINPUTS?180:130);
			winwidth = 350;
		}
		var linkInformation = getSelectedLink();
		var params = "?showCss=" + USE_LINKSTYLEINPUTS;
		if (linkInformation != null) {
			if (linkType == "link") {
				params += "&href=" + linkInformation["href"];
				params += "&target=" + linkInformation["target"];
				params += "&title= "+linkInformation["title"];
			} else {
				params += "&name=" + linkInformation["name"];
			}
			if (USE_LINKSTYLEINPUTS) {
				params += "&style=" + linkInformation["style"];
				params += "&class=" + linkInformation["class"];
			}
		}
		openWindow = window.open(workplacePath + "editors/dialogs/" + linkType + ".jsp" + params, "SetLink", "width=" + winwidth + ", height=" + winheight + ", resizable=yes, top=300, left=250");
		openWindow.focus();
    } else {
    	alert(errorMessage);
    }
}

// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
	FCK.InsertHtml(htmlContent);
}

// checks if a text part has been selected by the user
function hasSelectedText() {
	var sel;
	if (FCKBrowserInfo.IsIE) {
      sel = FCK.EditorWindow.selection;     
   } else {
      sel = FCK.EditorWindow.getSelection();       
   }
   if ((FCKSelection.GetType() == 'Text' || FCKSelection.GetType() == 'Control') && sel != '') {
        return true;
   }
   return false; 
}

// gets the selected html parts
function getSelectedHTML() {
   return FCK.EditorWindow.getSelection();
} 

// retrieves the information about the selected link
function getSelectedLink() {
	var linkInformation = null;

	// get the element of the current selection
	var thelink = FCK.Selection.GetSelectedElement();
	if (! thelink) {
		// failed, try to get the parent element
		thelink = FCK.Selection.GetParentElement();
	}
	if (thelink) {
    	if (/^img$/i.test(thelink.tagName)) {
       		thelink = thelink.parentNode;
    	}
    	if (!/^a$/i.test(thelink.tagName)) {
       		thelink = null;
    	}
  	}

	if (thelink != null) {
		var linkUri = thelink.getAttribute("href", 0);
		linkInformation = new Object();
		linkInformation["href"] = encodeURIComponent(linkUri);
		linkInformation["name"] = thelink.name;
		linkInformation["target"] = thelink.target;
		linkInformation["title"] = thelink.title;
		if (USE_LINKSTYLEINPUTS) {
			linkInformation["class"] = thelink.className;
		}	   
	}
	return linkInformation;
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {

    var a = FCK.Selection.MoveToAncestorNode('A') ;
    if (a) {
    	// link present, manipulate it
        FCK.Selection.SelectNode(a);
        //a.href= linkInformation["href"];
		a = FCK.CreateLink(linkInformation["href"]);
    } else {
    	// new link, create it
        a = FCK.CreateLink(linkInformation["href"]);
        
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
	
	if (USE_LINKSTYLEINPUTS) {
		if (linkInformation["class"] != "") {
			a.setAttribute("class", linkInformation["class"]);
		} else {
			a.removeAttribute("class");
		}
	}
} 
 
// OpenCms exit button
var exitCommand = function() { this.Name = 'Exit'; }
exitCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF; }
exitCommand.prototype.Execute = function() {
	if (!FCK.IsDirty() || confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
		execAction(FCK.LinkedField.form, '<%= CmsEditor.EDITOR_EXIT %>','_top');
	}
}
FCKCommands.RegisterCommand('oc-exit', new exitCommand());
FCKToolbarItems.RegisterItem('oc-exit', new FCKToolbarButton('oc-exit','<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0) %>', null, null, true));

// OpenCms publish button
var saveActionCommand = function() { this.Name = 'SaveAction'; }
saveActionCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF; }
saveActionCommand.prototype.Execute = function() {
	execAction(FCK.LinkedField.form, '<%= CmsEditor.EDITOR_SAVEACTION %>','_top');
}
FCKCommands.RegisterCommand('oc-publish', new saveActionCommand());
FCKToolbarItems.RegisterItem('oc-publish', new FCKToolbarButton('oc-publish','<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EXPLORER_CONTEXT_PUBLISH_0)  %>', null, null, true));

// OpenCms save button
var saveCommand = function() { this.Name = 'Save'; }
saveCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF; }
saveCommand.prototype.Execute = function() {
	execAction(FCK.LinkedField.form, '<%= CmsEditor.EDITOR_SAVE %>','_top');
}
FCKCommands.RegisterCommand('oc-save', new saveCommand());
FCKToolbarItems.RegisterItem('oc-save', new FCKToolbarButton('oc-save','<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0) %>', null, null, true));

// OpenCms save and exit button
var saveExitCommand = function() { this.Name = 'SaveExit'; }
saveExitCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF; }
saveExitCommand.prototype.Execute = function() {
	execAction(FCK.LinkedField.form, '<%= CmsEditor.EDITOR_SAVEEXIT %>','_top');
}
FCKCommands.RegisterCommand('oc-save_exit', new saveExitCommand());
FCKToolbarItems.RegisterItem('oc-save_exit', new FCKToolbarButton('oc-save_exit','<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0) %>', null, null, true));

// OpenCms link dialog button
var linkCommand = function() { this.Name = 'OcmsLink'; }
linkCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF ; }
linkCommand.prototype.Execute = function() { 
    openLinkDialog("<%= wp.key(org.opencms.workplace.editors.Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0) %>");
}
FCKCommands.RegisterCommand('oc-link', new linkCommand());
FCKToolbarItems.RegisterItem('oc-link', new FCKToolbarButton('oc-link','<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0) %>', null, null, true)); 

// OpenCms help button
var helpCommand = function() { this.Name = 'OcmsHelp'; }
helpCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF ; }
helpCommand.prototype.Execute = function() { 
    openOnlineHelp("/system/modules/org.opencms.editors.fckeditor/help/<%= wp.getLocale() %>/index.html");
}
FCKCommands.RegisterCommand('oc-help', new helpCommand());
FCKToolbarItems.RegisterItem('oc-help', new FCKToolbarButton('oc-help','<%=  wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0) %>', null, null, true)); 


// OpenCms gallery buttons
<%= wp.buildGalleryButtons(null, 0, null) %>