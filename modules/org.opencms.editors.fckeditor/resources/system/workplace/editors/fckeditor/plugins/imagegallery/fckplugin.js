<%@ page import="org.opencms.jsp.*,
                 org.opencms.workplace.*,
				 org.opencms.main.*,
				 org.opencms.file.types.CmsResourceTypeImage" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);	
	pageContext.setAttribute("cms", cms);
	CmsDialog dialog = new CmsDialog(pageContext, request, response);
	pageContext.setAttribute("locale", dialog.getLocale().toString());
    int imageResType = OpenCms.getResourceManager().getResourceType(CmsResourceTypeImage.getStaticTypeName()).getTypeId();
%>

<fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.ade.messagesADE">

// register the related commands
FCKCommands.RegisterCommand(
	"OcmsImageGallery",
	new FCKDialogCommand(
		"OcmsImageGallery",
		"<fmt:message key="GUI_EDITOR_TITLE_IMAGEGALLERY_0" />",
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

	var searchParam = "";
	var jsonQueryData = "{'querydata':{'types':[" + <%=imageResType %> + "],'galleries':[],'categories':[],'matchesperpage':12,'query':'','tabid':'cms_tab_results','page':1},'types':[" + <%=imageResType%> + "]}";
    var jsonTabsConfig = "['cms_tab_galleries','cms_tab_categories','cms_tab_search']";
	var jsonImageData = "{'widgetmode': 'simple','imgwidth':'','scale':'','showformats':true,'imgheight':'','useformats':false}";
    searchParam += "&data=" + jsonQueryData;
	searchParam += "&tabs=" + jsonTabsConfig;
	searchParam += "&imagedata=" + jsonImageData;
	return "<%= cms.link("/system/workplace/editors/ade/galleries.jsp") %>?dialogmode=editor&integrator=fckeditor/plugins/imagegallery/integrator.js" + searchParam + resParam;
}


// create the "OcmsImageGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageGalleryItem = new FCKToolbarButton("OcmsImageGallery", "<fmt:message key="GUI_EDITOR_TITLE_IMAGEGALLERY_0" />", "<fmt:message key="GUI_EDITOR_TOOLTIP_IMAGEGALLERY_0" />", null, false, true);
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
                        menu.AddItem("OcmsImageGallery", "<fmt:message key="GUI_EDITOR_TITLE_IMAGEGALLERY_0" />", 37);
                }
        }
    }
);
</fmt:bundle>