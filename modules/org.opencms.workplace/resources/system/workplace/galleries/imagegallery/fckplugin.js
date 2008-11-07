<%@ page import="org.opencms.workplace.galleries.*" %><%

CmsImageGalleryExtended wp = new CmsImageGalleryExtended(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsImageGallery",
	new FCKDialogCommand(
		"OcmsImageGallery",
		"<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>",
		imageGalleryDialogUrl(),
		680,
		750
	)
);

// create the path to the image gallery dialog with some request parameters for the dialog
function imageGalleryDialogUrl() {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
	}
	return FCKPlugins.Items["imagegallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsImageGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageGalleryItem = new FCKToolbarButton("OcmsImageGallery", "<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_IMAGEGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsImageGalleryItem.IconPath = 37;

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