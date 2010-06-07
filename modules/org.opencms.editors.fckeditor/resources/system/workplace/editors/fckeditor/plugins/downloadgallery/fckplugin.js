<%@ page import="org.opencms.jsp.*,
                 org.opencms.workplace.*,                 
				 org.opencms.main.*,
				 org.opencms.file.types.CmsResourceTypeBinary" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);	
	pageContext.setAttribute("cms", cms);
	CmsDialog dialog = new CmsDialog(pageContext, request, response);
	pageContext.setAttribute("locale", dialog.getLocale().toString());
    String itemResType = CmsResourceTypeBinary.getStaticTypeName();
%><fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.ade.messagesADE">
// nesting a FCKDialogCommand to use dynamic the dialog URLs
var dialogCommand = function() { this.Name = "OcmsDownloadGallery"; }
dialogCommand.prototype.GetState = function() { return FCK_TRISTATE_OFF; }
dialogCommand.prototype.Execute = function() {
	var command=new FCKDialogCommand(
			"OcmsDownloadGallery",
			"<fmt:message key="GUI_EDITOR_TITLE_DOWNLOADGALLERY_0" />",
			downloadGalleryDialogUrl(),
	        685,
			566
		);
	command.Execute();
}
//register the related commands
FCKCommands.RegisterCommand(
		"OcmsDownloadGallery",
		new dialogCommand());

//checks if a text part has been selected by the user
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

// Searches for a frame by the specified name. Will only return siblings or ancestors.
function getFrame(startFrame, frameName){
    if (startFrame == top){
        if (startFrame.name == frameName){
            return startFrame;
        }
        return null;
    }
    for (var i=0; i<startFrame.parent.frames.length; i++){
        if (startFrame.parent.frames[i].name == frameName) {
            return startFrame.parent.frames[i];
        }
    }
    return getFrame(startFrame.parent, frameName);
}

// create the path to the image gallery dialog with some request parameters for the dialog
function downloadGalleryDialogUrl() {
	var path="";
	if (hasSelectedText() == true) {
		var a = FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
    		FCK.Selection.SelectNode(a);
        	//path to resource
    		path = a.getAttribute("_fcksavedurl");
        }	
	}
	var resParam = "";
    var editFrame=getFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	var searchParam = "&types=<%=itemResType %>&currentelement="+path;
	return "<%= cms.link("/system/modules/org.opencms.ade.galleries/gallery.jsp") %>?dialogmode=editor" + searchParam + resParam;
}


// create the "OcmsDownloadGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton("OcmsDownloadGallery", "<fmt:message key="GUI_EDITOR_TITLE_DOWNLOADGALLERY_0" />", "<fmt:message key="GUI_EDITOR_TOOLTIP_DOWNLOADGALLERY_0" />", null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

// "OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);
</fmt:bundle>