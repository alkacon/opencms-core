<%@ page import="
	java.util.*,
	org.opencms.editors.ckeditor.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	org.opencms.util.*,
	org.opencms.widgets.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*"
%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsCKEditor wp = new CmsCKEditor(cms);

String site = OpenCms.getSiteManager().getWorkplaceServer();

String configuration = request.getParameter(CmsCKEditorWidget.PARAM_CONFIGURATION);
CmsHtmlWidgetOption option = new CmsHtmlWidgetOption(configuration);

String galleryPath = cms.link("/system/workplace/galleries/");

%>CKEDITOR.editorConfig = function(config)
{

config.language = "<%= wp.getLocale().getLanguage() %>",
config.defaultLanguage = '<%= Locale.ENGLISH %>',

config.width = "100%",
config.height = "<%= option.getEditorHeight() %>",
config.resize_enabled = false,

config.enterMode = CKEDITOR.ENTER_P,
config.shiftEnterMode = CKEDITOR.ENTER_BR,

config.entities = true,
config.entities_processNumerical = false,
config.entities_latin = false,
config.entities_greek = false,

config.pasteFromWordIgnoreFontFace = true,
config.pasteFromWordKeepsStructure = false,
config.pasteFromWordRemoveStyle = true,

<%= CmsCKEditorWidget.getFormatSelectOptionsConfiguration(option) %>

config.baseHref = "<%= site %>",
config.toolbarCanCollapse = true,
config.toolbarStartupExpanded = false,

config.skin = "opencms,<%= wp.getEditorResourceUri() %>skins/opencms/",

// overwrite default menu groups because of image context menu (replace "image" by "imageocms")
config.menu_groups = "clipboard,form,tablecell,tablecellproperties,tablerow,tablecolumn,table,anchor,link,imageocms,flash," +
	"checkbox,radio,textfield,hiddenfield,imagebutton,button,select,textarea",

config.removePlugins = "image,elementspath,save,resize",

config.extraPlugins = "opencms,iframedialog,imagegallery,downloadgallery,linkgallery,htmlgallery,tablegallery",

config.toolbar = [
        <%= option.getButtonBar(CmsCKEditorWidget.BUTTON_TRANSLATION_MAP, ",") %>
],

config.keystrokes = [
	[ CKEDITOR.CTRL + 65 /*A*/, true ],
	[ CKEDITOR.CTRL + 67 /*C*/, true ],
	[ CKEDITOR.CTRL + 70 /*F*/, true ],
	[ CKEDITOR.CTRL + 83 /*S*/, true ],
	[ CKEDITOR.CTRL + 88 /*X*/, true ],<%
	if (!option.isButtonHidden("paste")) { %>
	[ CKEDITOR.CTRL + 86 /*V*/, 'paste' ],
	[ CKEDITOR.SHIFT + 45 /*INS*/, 'paste' ],<% } %>
	[ CKEDITOR.CTRL + 90 /*Z*/, 'undo' ],
	[ CKEDITOR.CTRL + 89 /*Y*/, 'redo' ],
	[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 90 /*Z*/, 'redo' ],<% 
	if (option.showLinkDialog()) { %>
	[ CKEDITOR.CTRL + 76 /*L*/, 'link' ],<% }
	if (!option.isButtonHidden("bold")) { %>
	[ CKEDITOR.CTRL + 66 /*B*/, 'bold' ],<% }
	if (!option.isButtonHidden("italic")) { %>
	[ CKEDITOR.CTRL + 73 /*I*/, 'italic' ],<% }
	if (!option.isButtonHidden("underline")) { %>
	[ CKEDITOR.CTRL + 85 /*U*/, 'underline' ],<% } %>
	[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 83 /*S*/, true ],
	[ CKEDITOR.CTRL + CKEDITOR.ALT + 13 /*ENTER*/, 'fitWindow' ]<% 
	if (option.showSourceEditor()) { %>,
	[ CKEDITOR.CTRL + 9 /*TAB*/, 'source' ]<%
	} %>
] 

};