<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxTableGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsTableGallery",
	new FCKDialogCommand(
		"OcmsTableGallery",
		"<%= wp.key(Messages.GUI_TABLEGALLERY_EDITOR_TITLE_0) %>",
		tableGalleryDialogUrl(),
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
function tableGalleryDialogUrl() {
	var resParam = "";
    var editFrame=findFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	return FCKPlugins.Items["tablegallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsTableGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsTableGalleryItem = new FCKToolbarButton("OcmsTableGallery", "<%= wp.key(Messages.GUI_TABLEGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_TABLEGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsTableGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-tablegallery.gif";

// "OcmsTableGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsTableGallery", opencmsTableGalleryItem);
