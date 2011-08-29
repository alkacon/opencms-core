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
<fmt:bundle basename="org.opencms.ade.galleries.messages">
// nesting a FCKDialogCommand to use dynamic the dialog URLs

/**
 * The dialog command constructor.<p>
 */
var dialogCommand = function() {
	this.Name = "OcmsDownloadGallery";
}

/**
 * Returns the command state.<p>
 * 
 * @return the state
 */
dialogCommand.prototype.GetState = function() { 
	return FCK_TRISTATE_OFF;
}

/**
 * Executes the command.<p>
 * 
 * @return void
 */
dialogCommand.prototype.Execute = function() {
	var command=new FCKDialogCommand(
			'OcmsDownloadGallery',
			'<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
			downloadGalleryDialogUrl(),
			685,
			577
		);
	command.Execute();
}

//register the related commands
FCKCommands.RegisterCommand(
		"OcmsDownloadGallery",
		new dialogCommand());

//create the "OcmsDownloadGallery" toolbar button
//syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton('OcmsDownloadGallery', '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />', '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />', null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

//"OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);

/**
 * Returns if there is text selected within the FCKEditor.<p>
 * 
 * @return <code>boolean</code> <code>true</code> if text is selected
 */
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

/**
 * Searches for a frame by the specified name. Will only return siblings or ancestors.<p>
 * 
 * @return <code>Frame</code> the frame or <code>null</code> if no matching frame is found
 */ 
function findFrame(startFrame, frameName){
    if (startFrame == top){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.name == frameName){
                return startFrame;
            }
        }catch(err){}
        return null;
    }
    for (var i=0; i<startFrame.parent.frames.length; i++){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.parent.frames[i].name == frameName) {
                return startFrame.parent.frames[i];
            }
        }catch(err){}
    }
    return findFrame(startFrame.parent, frameName);
}

/**
 * Returns the path to the download gallery dialog with some request parameters for the dialog.<p>
 * 
 * @return <code>String</code> the dialog URL
 */ 
function downloadGalleryDialogUrl() {
	var path=null;
	if (hasSelectedText() == true) {
		var a = FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
    		FCK.Selection.SelectNode(a);
        	//path to resource
    		path = a.getAttribute("_fcksavedurl");
    		
    		// in case of a newly created link, use the href attribute
    		if (path == null || path==""){
    			path=a.getAttribute("href");
    		}
        }
	}
	var resParam = "";
    var editFrame=findFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	// set the content locale
    var elementLanguage="${locale}";
    try{
        elementLanguage=editFrame.editform.document.forms['EDITOR']['elementlanguage'].value;
    }catch(err){
        // nothing to do
    }
    var searchParam = "&types=<%=itemResType %>&currentelement="+ ( path==null ? "" : path)+"&__locale="+elementLanguage;
	return "<%= cms.link("/system/modules/org.opencms.ade.galleries/gallery.jsp") %>?dialogmode=editor" + searchParam + resParam;
}
</fmt:bundle>