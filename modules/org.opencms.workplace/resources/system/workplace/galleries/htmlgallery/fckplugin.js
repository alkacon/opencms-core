<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxHtmlGallery(pageContext, request, response);

%>
// register the related commands
FCKCommands.RegisterCommand(
	"OcmsHtmlGallery",
	new FCKDialogCommand(
		"OcmsHtmlGallery",
		"<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TITLE_0) %>",
		htmlGalleryDialogUrl(),
		680,
		630
	)
);

// create the path to the item gallery dialog with some request parameters for the dialog
function htmlGalleryDialogUrl() {
	var resParam = "";
	if (top.edit.editedResource != null) {
		resParam = "&resource=" + top.edit.editedResource;
		
	} else {
		resParam = "&resource=" + top.edit.editform.editedResource;
	}
	return FCKPlugins.Items["htmlgallery"].Path + "index.jsp?dialogmode=editor" + resParam;
}

// create the "OcmsHtmlGallery" toolbar button
// syntax: FCKToolbarButton(commandName, label, tooltip, style, sourceView, contextSensitive) 
var opencmsHtmlGalleryItem = new FCKToolbarButton("OcmsHtmlGallery", "<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TITLE_0) %>", "<%= wp.key(Messages.GUI_HTMLGALLERY_EDITOR_TOOLTIP_0) %>", null, false, true);
opencmsHtmlGalleryItem.IconPath = FCKConfig.SkinPath + "toolbar/oc-htmlgallery.gif";

// "OcmsHtmlGallery" is the name that is used in the toolbar configuration
FCKToolbarItems.RegisterItem("OcmsHtmlGallery", opencmsHtmlGalleryItem);
