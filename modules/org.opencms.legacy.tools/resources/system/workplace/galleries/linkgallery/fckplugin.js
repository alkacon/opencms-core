<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxLinkGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsLinkGallery",
	new FCKDialogCommand(
		"OcmsLinkGallery",
		"<%= wp.key(Messages.GUI_LINKGALLERY_EDITOR_TITLE_0) %>",
		linkGalleryDialogUrl(),
		680,
		630
	)
);

// Searches for a frame by the specified name. Will only return siblings or ancestors.
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

// create the path to the item gallery dialog with some request parameters for the dialog
function linkGalleryDialogUrl() {
	var resParam = "";
    var editFrame=findFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	return FCKPlugins.Items["linkgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsLinkGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsLinkGalleryItem = new FCKToolbarButton("OcmsLinkGallery", "<%= wp.key(Messages.GUI_LINKGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsLinkGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-linkgallery.gif";

// "OcmsLinkGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsLinkGallery", opencmsLinkGalleryItem);
