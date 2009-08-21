<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxHtmlGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsHtmlGallery",
	new FCKDialogCommand(
		"OcmsHtmlGallery",
		"<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TITLE_0) %>",
		htmlGalleryDialogUrl(),
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
function htmlGalleryDialogUrl() {
	var resParam = "";
    var editFrame=getFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	return FCKPlugins.Items["htmlgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsHtmlGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsHtmlGalleryItem = new FCKToolbarButton("OcmsHtmlGallery", "<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsHtmlGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-htmlgallery.gif";

// "OcmsHtmlGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsHtmlGallery", opencmsHtmlGalleryItem);
