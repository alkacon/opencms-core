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

// create the path to the item gallery dialog with some request parameters for the dialog
function tableGalleryDialogUrl() {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
		
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	return FCKPlugins.Items["tablegallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsTableGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsTableGalleryItem = new FCKToolbarButton("OcmsTableGallery", "<%= wp.key(Messages.GUI_TABLEGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_TABLEGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsTableGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-tablegallery.gif";

// "OcmsTableGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsTableGallery", opencmsTableGalleryItem);
