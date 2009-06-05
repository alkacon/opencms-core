<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxDownloadGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsDownloadGallery",
	new FCKDialogCommand(
		"OcmsDownloadGallery",
		"<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TITLE_0) %>",
		downloadGalleryDialogUrl(),
		680,
		630
	)
);

// create the path to the item gallery dialog with some request parameters for the dialog
function downloadGalleryDialogUrl() {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
		
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	return FCKPlugins.Items["downloadgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsDownloadGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsDownloadGalleryItem = new FCKToolbarButton("OcmsDownloadGallery", "<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_DOWNLOADGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsDownloadGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-downloadgallery.gif";

// "OcmsDownloadGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsDownloadGallery", opencmsDownloadGalleryItem);
