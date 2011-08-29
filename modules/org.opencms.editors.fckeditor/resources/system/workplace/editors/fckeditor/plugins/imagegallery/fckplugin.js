<%@ page import="org.opencms.jsp.*,
                 org.opencms.workplace.*,                 
				 org.opencms.main.*,
				 org.opencms.file.types.CmsResourceTypeImage" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);	
	pageContext.setAttribute("cms", cms);
	CmsDialog dialog = new CmsDialog(pageContext, request, response);
	pageContext.setAttribute("locale", dialog.getLocale().toString());
    String itemResType = CmsResourceTypeImage.getStaticTypeName();
%><fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.ade.galleries.messages">
// nesting a FCKDialogCommand to use dynamic the dialog URLs

/**
 * The dialog command constructor.<p>
 */
var dialogCommand = function() {
	this.Name = "OcmsImageGallery";
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
			'OcmsImageGallery',
			'<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
			imageGalleryDialogUrl(),
			685,
			577
		);
	command.Execute();
}

//register the related commands
FCKCommands.RegisterCommand(
		"OcmsImageGallery",
		new dialogCommand());

//create the "OcmsImageGallery" toolbar button
//syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsImageGalleryItem = new FCKToolbarButton('OcmsImageGallery', '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />', '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />', null, false, true);
opencmsImageGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-imagegallery.gif";

//"OcmsImageGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsImageGallery", opencmsImageGalleryItem);

//create the new context menu for image tags
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
            menu.AddItem( "Cut"    , FCKLang.Cut    , 7, FCKCommands.GetCommand('Cut').GetState() == FCK_TRISTATE_DISABLED ) ;
            menu.AddItem( "Copy"    , FCKLang.Copy    , 8, FCKCommands.GetCommand('Copy').GetState() == FCK_TRISTATE_DISABLED ) ;
            menu.AddItem( "Paste"    , FCKLang.Paste    , 9, FCKCommands.GetCommand('Paste').GetState() == FCK_TRISTATE_DISABLED ) ;
                        // add a separator
                        menu.AddSeparator() ;
                        // the command needs the registered command name, the title for the context menu, and the icon path
                        menu.AddItem("OcmsImageGallery", "<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />", 37);
                }
        }
    }
); 

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
function imageGalleryDialogUrl() {
	var path=null;
	var selected=FCK.Selection.GetSelectedElement();
	if (selected && ( selected.tagName == "IMG" || selected.tagName == "SPAN" || (selected.tagName == "INPUT" && selected.type == "image"))){
		// try to read selected url
		path = selected.getAttribute("_fcksavedurl");
		if (path == null) {
			path = GetAttribute(selected, "src", "");
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