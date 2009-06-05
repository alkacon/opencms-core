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

// create the path to the item gallery dialog with some request parameters for the dialog
function linkGalleryDialogUrl() {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
		
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	return FCKPlugins.Items["linkgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsLinkGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsLinkGalleryItem = new FCKToolbarButton("OcmsLinkGallery", "<%= wp.key(Messages.GUI_LINKGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsLinkGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-linkgallery.gif";

// "OcmsLinkGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsLinkGallery", opencmsLinkGalleryItem);
