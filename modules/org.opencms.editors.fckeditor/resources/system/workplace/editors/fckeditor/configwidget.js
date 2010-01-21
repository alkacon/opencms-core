<%@ page import="
    java.util.*,
	org.opencms.editors.fckeditor.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	org.opencms.util.*,
	org.opencms.widgets.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*"
%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsDialog wp = new CmsDialog(cms);

String site = OpenCms.getSiteManager().getWorkplaceServer();

String configuration = request.getParameter(CmsFCKEditorWidget.PARAM_CONFIGURATION);
CmsHtmlWidgetOption option = new CmsHtmlWidgetOption(configuration);

//the editor options
CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

String galleryPath = cms.link("/system/workplace/galleries/");

%>
FCKConfig.AutoDetectLanguage = false;
FCKConfig.DefaultLanguage = "<%= wp.getLocale().getLanguage() %>";

FCKConfig.CustomStyles = {};

FCKConfig.ProcessHTMLEntities = true;
FCKConfig.ProcessNumericEntities = false;
FCKConfig.IncludeLatinEntities = false;
FCKConfig.IncludeGreekEntities = false;

//set flags to set enhanced options and thickbox class for imagegallery
FCKConfig.ShowEnhancedOptions = <%=options.showElement("gallery.enhancedoptions", displayOptions) %>;
FCKConfig.UseTbForLinkOriginal = <%=options.showElement("gallery.usethickbox", displayOptions) %>;

<%= CmsFCKEditorWidget.getFormatSelectOptionsConfiguration(option) %>

FCKConfig.BaseHref = "<%= site %>";

FCKConfig.ToolbarCanCollapse = true;
FCKConfig.ToolbarStartExpanded	= false;

FCKConfig.SkinPath = FCKConfig.BasePath + "skins/opencms/";

<% if (options.showElement("gallery.advanced", displayOptions)){ %>

    FCKConfig.Plugins.Add("opencms", null, "<%= cms.link("plugins/") %>");<%
    if (option.isButtonAdditional("imagegallery") || option.isButtonAdditional("image")) { %>
    FCKConfig.Plugins.Add("imagegallery", null, "<%= cms.link("plugins/") %>");<% }
    if (option.isButtonAdditional("downloadgallery")) { %>
    FCKConfig.Plugins.Add("downloadgallery", null, "<%= cms.link("plugins/") %>");<% }
    if (option.isButtonAdditional("linkgallery")) { %>
    FCKConfig.Plugins.Add("linkgallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("htmlgallery")) { %>
    FCKConfig.Plugins.Add("htmlgallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("tablegallery")) { %>
    FCKConfig.Plugins.Add("tablegallery", null, "<%= galleryPath %>");<% } 
} else {%>
    FCKConfig.Plugins.Add("opencms", null, "<%= cms.link("plugins/") %>");<%
    if (option.isButtonAdditional("imagegallery") || option.isButtonAdditional("image")) { %>
    FCKConfig.Plugins.Add("imagegallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("downloadgallery")) { %>
    FCKConfig.Plugins.Add("downloadgallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("linkgallery")) { %>
    FCKConfig.Plugins.Add("linkgallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("htmlgallery")) { %>
    FCKConfig.Plugins.Add("htmlgallery", null, "<%= galleryPath %>");<% }
    if (option.isButtonAdditional("tablegallery")) { %>
    FCKConfig.Plugins.Add("tablegallery", null, "<%= galleryPath %>");<% } 
 } %> 

FCKConfig.ToolbarSets["OpenCmsWidget"] = [
        <%= option.getButtonBar(CmsFCKEditorWidget.BUTTON_TRANSLATION_MAP, ",") %>
];

FCKConfig.Keystrokes = [
	[ CTRL + 65 /*A*/, true ],
	[ CTRL + 67 /*C*/, true ],
	[ CTRL + 70 /*F*/, true ],
	[ CTRL + 83 /*S*/, true ],
	[ CTRL + 88 /*X*/, true ],<%
	if (!option.isButtonHidden("paste")) { %>
	[ CTRL + 86 /*V*/, 'Paste' ],
	[ SHIFT + 45 /*INS*/, 'Paste' ],<% } %>
	[ CTRL + 90 /*Z*/, 'Undo' ],
	[ CTRL + 89 /*Y*/, 'Redo' ],
	[ CTRL + SHIFT + 90 /*Z*/, 'Redo' ],<% 
	if (option.showLinkDialog()) { %>
	[ CTRL + 76 /*L*/, 'Link' ],<% }
	if (!option.isButtonHidden("bold")) { %>
	[ CTRL + 66 /*B*/, 'Bold' ],<% }
	if (!option.isButtonHidden("italic")) { %>
	[ CTRL + 73 /*I*/, 'Italic' ],<% }
	if (!option.isButtonHidden("underline")) { %>
	[ CTRL + 85 /*U*/, 'Underline' ],<% } %>
	[ CTRL + SHIFT + 83 /*S*/, true ],
	[ CTRL + ALT + 13 /*ENTER*/, 'FitWindow' ]<% 
	if (option.showSourceEditor()) { %>,
	[ CTRL + 9 /*TAB*/, 'Source' ]<%
	} %>
] ;

FCKConfig.PreserveSessionOnFileBrowser = true;

FCKConfig.ImageUpload = false;
FCKConfig.ImageBrowserURL = FCKConfig.BasePath + "filemanager/browser/default/browser.html?Type=Image&Connector=<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp") %>";
FCKConfig.ImageUploadURL = "<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp?Type=Image") %>";
FCKConfig.ImageBrowserWindowWidth  = 700;
FCKConfig.ImageBrowserWindowHeight = 500;

FCKConfig.LinkUpload = false;
FCKConfig.LinkBrowserURL = FCKConfig.BasePath + "filemanager/browser/default/browser.html?Connector=<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp") %>";
FCKConfig.LinkUploadURL = "<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp") %>";
FCKConfig.LinkBrowserWindowWidth  = 700;
FCKConfig.LinkBrowserWindowHeight = 500;