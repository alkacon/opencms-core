<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%>

var fckEditorType

// register the related commands
FCKCommands.RegisterCommand(
	"OcmsImageGallery",
	new FCKDialogCommand(
		"OcmsImageGallery",
		"TODO_Title",
		imageGalleryDialogUrl(),
		685,
		566
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

// create the path to the image gallery dialog with some request parameters for the dialog
function imageGalleryDialogUrl() {
	var resParam = "";
    	var editFrame=getFrame(self, 'edit');
	if (editFrame.editedResource != null) {
		resParam = "&resource=" + editFrame.editedResource;
		
	} else {
		resParam = "&resource=" + editFrame.editform.editedResource;
	}

	var dataParam = "";
	var jsonQueryData = "{'querydata':{'types':[3],'galleries':[],'categories':[],'matchesperpage':8,'query':'','tabid':'tabs-results','page':1},'types':[3]}";
	dataParam += "&data=" + jsonQueryData;
	var jsonImageData = "{'widgetmode': 'simple','imgwidth':'','scale':'','showformats':true,'imgheight':'','useformats':false}";
	dataParam += "&imagedata=" + jsonImageData;
	return "<%= cms.link("/system/workplace/editors/ade/galleries.jsp") %>?dialogmode=editor&integrator=fckeditor/plugins/imagegallery/integrator.js" + dataParam + resParam;
}


// create the "OcmsImageGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageGalleryItem = new FCKToolbarButton("OcmsImageGallery", "TODO_Title", "TODO_Tooltip", null, false, true);
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
                        menu.AddItem("OcmsImageGallery", "TODO_title", 37);
                }
        }
    }
);

function getFCKEditor() {
	//var dialog		= window.parent;
	//var oEditor		= dialog.InnerDialogLoaded();
	return oEditor;
}