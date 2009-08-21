<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxDownloadGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsDownloadGallery",
	new FCKDialogCommand(
		"OcmsDownloadGallery",
		"<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TITLE_0) %>",
		downloadGalleryDialogUrl(),
		680,
		630
	)
);

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

// create the path to the item gallery dialog with some request parameters for the dialog
function downloadGalleryDialogUrl() {
	var resParam = "";
    var editFrame=getFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	return FCKPlugins.Items["downloadgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsDownloadGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton("OcmsDownloadGallery", "<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

// "OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);
