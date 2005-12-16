<%@ page import="java.util.*, 
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

String site = OpenCms.getSiteManager().getCurrentSite(cms.getCmsObject()).getUrl();

String configuration = request.getParameter(CmsFCKEditorWidget.PARAM_CONFIGURATION);
if (CmsStringUtil.isEmpty(configuration)) {
	configuration = "";
}

%>
FCKConfig.AutoDetectLanguage = false;
FCKConfig.DefaultLanguage = "<%= wp.getLocale().getLanguage() %>";

FCKConfig.BaseHref = "<%= site %>";

FCKConfig.ToolbarCanCollapse = true;
FCKConfig.ToolbarStartExpanded	= false;

FCKConfig.SkinPath = FCKConfig.BasePath + "skins/opencms/";

FCKConfig.Plugins.Add("opencms", null, "<%= cms.link("plugins/") %>");
<%

StringBuffer toolbar = new StringBuffer(1024);

toolbar.append("[");

toolbar.append("'Undo','Redo','-','SelectAll','RemoveFormat'");

toolbar.append(",'-','Cut','Copy','Paste','PasteText','PasteWord'");

toolbar.append(",'-','Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'");

toolbar.append(",'-','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'");

toolbar.append(",'-','OrderedList','UnorderedList'");

toolbar.append(",'-','Outdent','Indent'");

toolbar.append("]");

// append customized OpenCms buttons
if (CmsFCKEditorWidget.buildOpenCmsButtonRow(toolbar, configuration)) {
	toolbar.append(",'-',");
} else {
	toolbar.append(",[");
}

toolbar.append("'SpecialChar','UniversalKey'");

toolbar.append(",'-','Print']");

%>
FCKConfig.ToolbarSets["OpenCmsWidget"] = [
        <%= toolbar %>
];

FCKConfig.ImageUpload = false;
FCKConfig.ImageBrowserURL = FCKConfig.BasePath + "filemanager/browser/default/browser.html?Type=Image&Connector=<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp") %>";
FCKConfig.ImageUploadURL = "<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp?Type=Image") %>";
FCKConfig.ImageBrowserWindowWidth  = 700;
FCKConfig.ImageBrowserWindowHeight = 500;