<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsImageGallery",
	new FCKDialogCommand(
		"OcmsImageGallery",
		"<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>",
		imageGalleryDialogUrl(),
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

// create the path to the image gallery dialog with some request parameters for the dialog
function imageGalleryDialogUrl() {
	var resParam = "";
    var editFrame=findFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}
	
	return FCKPlugins.Items["imagegallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}


// create the "OcmsImageGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageGalleryItem = new FCKToolbarButton("OcmsImageGallery", "<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsImageGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-imagegallery.gif";;

// "OcmsImageGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsImageGallery", opencmsImageGalleryItem);

// create the new context menu for image tags
FCK.ContextMenu.RegisterListener(
    {
        AddItems : function( menu, tag, tagName )
        {
                // display this menu for image tags
                if (tagName == "IMG"  && !tag.getAttribute("_fckfakelement"))
                {
			// first, we have to remove all original menu items to use the special image dialog
			menu.RemoveAllItems();

			// add the general items
			menu.AddItem( "Cut"	, FCKLang.Cut	, 7, FCKCommands.GetCommand('Cut').GetState() == FCK_TRISTATE_DISABLED ) ;
			menu.AddItem( "Copy"	, FCKLang.Copy	, 8, FCKCommands.GetCommand('Copy').GetState() == FCK_TRISTATE_DISABLED ) ;
			menu.AddItem( "Paste"	, FCKLang.Paste	, 9, FCKCommands.GetCommand('Paste').GetState() == FCK_TRISTATE_DISABLED ) ;
                        // add a separator
                        menu.AddSeparator() ;
                        // the command needs the registered command name, the title for the context menu, and the icon path
                        menu.AddItem("OcmsImageGallery", "<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>", 37);
                }
        }
    }
);