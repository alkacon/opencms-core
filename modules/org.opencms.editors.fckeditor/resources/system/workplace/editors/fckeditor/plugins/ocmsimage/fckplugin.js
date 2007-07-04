// register the related commands
FCKCommands.RegisterCommand(
	"OcmsImage",
	new FCKDialogCommand(
		"OcmsImage",
		FCKLang["OcmsImageDlgTitle"],
		FCKPlugins.Items["ocmsimage"].Path + "ocmsimage.html",
		680,
		620
	)
);

// create the "OcmsImage" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageItem = new FCKToolbarButton("OcmsImage", FCKLang["OcmsImageBtn"], FCKLang["OcmsImageTooltip"], null, false, true);
opencmsImageItem.IconPath = 37; //FCKPlugins.Items["ocmsimage"].Path + "images/toolbar/ocmsimage.gif";

// "OcmsImage" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsImage", opencmsImageItem);

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
                        menu.AddItem("OcmsImage", FCKLang["OcmsImageDlgTitle"], 37);
                }
        }
    }
);
