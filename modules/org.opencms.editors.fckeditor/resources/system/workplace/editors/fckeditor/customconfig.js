<%@ page import="
	java.util.*, 
	org.opencms.jsp.*, 
	org.opencms.main.*, 
	org.opencms.util.*, 
	org.opencms.workplace.editors.*, 
	org.opencms.editors.fckeditor.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsFCKEditor wp = new CmsFCKEditor(cms);

cms.getResponse().setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, "no-cache");

CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

// get editor configuration object from session, because request parameters do not work
CmsFCKEditorConfiguration extConf = CmsFCKEditorConfiguration.getConfiguration(session);

String cssPath = extConf.getUriStyleSheet();

// This editor supports user defined styles. To show these styles, a plain text file containing the style definition
// XML code has to be placed in the same folder where the template CSS style sheet is located.
// The file name has to be exactly like the file name of the CSS with the suffix "_style.xml" added. 
// E.g. for the CSS file "style.css" the style definition file has to be named "style.css_style.xml".
// An example for a style XML can be found in the VFS file "/system/workplace/resources/editors/fckeditor/fckstyles.xml". 
boolean styleXMLPresent = false;
if (CmsStringUtil.isNotEmpty(cssPath)) {
	String styleXML = cssPath + CmsFCKEditor.SUFFIX_STYLESXML;
	if (cms.getCmsObject().existsResource(styleXML)) {
		styleXMLPresent = true;
		%>FCKConfig.StylesXmlPath = "<%= cms.link(styleXML) %>";<%
	}
	%>FCKConfig.EditorAreaCSS = "<%= cms.link(cssPath) %>";<%
}

String resource = extConf.getResourcePath();

String site = OpenCms.getSiteManager().getWorkplaceServer();

%>
FCKConfig.AutoDetectLanguage = false;
FCKConfig.DefaultLanguage = "<%= wp.getLocale().getLanguage() %>";

FCKConfig.BaseHref = "<%= site %>";
FCKConfig.ToolbarCanCollapse = false;

FCKConfig.SkinPath = FCKConfig.BasePath + "skins/opencms/";

FCKConfig.Plugins.Add("opencms", null, "<%= cms.link("plugins/") %>");
FCKConfig.Plugins.Add("ocmsimage", "en,de", "<%= cms.link("plugins/") %>");
<%

boolean showTableOptions = options.showElement("option.table", displayOptions);

// show table commands if the user has the permission to edit tables
if (showTableOptions) {
  %>FCKConfig.Plugins.Add('tablecommands');<%
}

StringBuffer toolbar = new StringBuffer(2048);

toolbar.append("[");

if (CmsStringUtil.isNotEmpty(resource) && options.showElement("button.customized", displayOptions)) {
	I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
	if (actionClass.isButtonActive(wp.getJsp(), resource)) {
		toolbar.append("'oc-publish',");
	}
}

toolbar.append("'oc-save_exit','oc-save'");

// source code button
if (options.showElement("option.sourcecode", displayOptions)) {
	toolbar.append(",'-','Source'");
} 

// standard buttons: undo/redo, find, cut/copy/paste
toolbar.append(",'-','Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat','-','Cut','Copy','Paste','PasteText','PasteWord'");


// determine if the insert table button should be shown
if (showTableOptions) {
	toolbar.append(",'-','Table'");
	// toolbar.append(",'-','TableInsertRow','TableDeleteRows','TableInsertColumn','TableDeleteColumns','TableInsertCell','TableDeleteCells','TableMergeCells','TableSplitCell'");
}

// determine if the insert link buttons should be shown
if (options.showElement("option.links", displayOptions)) {
	toolbar.append(",'-','oc-link','Link', 'Anchor','Unlink'");
}        

// build the available gallery buttons
toolbar.append(wp.buildGalleryButtonRow(options, displayOptions));

// determine if the flash button button should be shown
if (options.showElement("option.flash", displayOptions)) {
	toolbar.append(",'-','Flash'");
}

// determine if the insert/edit image button should be shown
if (options.showElement("option.images", displayOptions)) {
	toolbar.append(",'-', 'OcmsImage'");
}

// insert rule button
toolbar.append(",'-','Rule'");

// determine if the insert special characters button should be shown
if (options.showElement("option.specialchars", displayOptions)) {
	toolbar.append(",'SpecialChar','UniversalKey'");
}

// insert print button
toolbar.append(",'-','Print'");

// determine if the spell check button should be shown
if (options.showElement("option.spellcheck", displayOptions)) {
	toolbar.append(",'SpellCheck'");
}

// determine if the help button should be shown
if (wp.isHelpEnabled()) {
	toolbar.append(",'-','oc-help'");
}

toolbar.append(",'-','oc-exit']");

// style buttons 
toolbar.append(", '/'\n,[");
toolbar.append("'FontFormat'");

boolean fontFace = options.showElement("font.face", displayOptions);
boolean fontSize = options.showElement("font.size", displayOptions);
boolean style = styleXMLPresent && options.showElement("option.style", displayOptions);

if (style || fontFace || fontSize) {
	// determine if the font face selector should be shown
	if (fontFace) {
		toolbar.append(",'FontName'");
	}

	// determine if the font size selector should be shown 
	if (fontSize) {
		toolbar.append(",'FontSize'");
	}


	// determine if the style selector should be shown
	if (style) {
		toolbar.append(",'Style'");
	}
}

// determine if the font decoration buttons should be shown
if (options.showElement("font.decoration", displayOptions)) {
	toolbar.append(",'Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'");
}

// determine if the text alignment buttons should be shown
if (options.showElement("text.align", displayOptions)) {
	toolbar.append(",'-','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'");
}       

// determine if the text list buttons should be shown
if (options.showElement("text.lists", displayOptions)) {
	toolbar.append(",'-','OrderedList','UnorderedList'");
}

// determine if the text indentation buttons should be shown 
if (options.showElement("text.indent", displayOptions)) {
	toolbar.append(",'-','Outdent','Indent'");
}   

// Determine wich color selectors should be shown
boolean fontColor = options.showElement("font.color", displayOptions);
boolean bgColor = options.showElement("bg.color", displayOptions);
if (fontColor || bgColor) {
    toolbar.append(",'-',");   
    if (fontColor && bgColor) {
      toolbar.append("'TextColor','BGColor'");
    } else {
    	if (fontColor) {
           toolbar.append("'TextColor'");
        }
        if (bgColor) {
           toolbar.append("'BGColor'");
        }
    }
}

toolbar.append("]");

// determines if the form editing buttons should be shown
if (options.showElement("option.form", displayOptions)) {
        toolbar.append(",['Form','-','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField']");
}

%>

FCKConfig.ToolbarSets["OpenCms"] = [
        <%= toolbar %>
];

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

FCKConfig.FlashUpload = false;
FCKConfig.FlashBrowserURL = FCKConfig.BasePath + "filemanager/browser/default/browser.html?Type=Flash&Connector=<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp") %>";
FCKConfig.FlashUploadURL = "<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/filebrowser/connector.jsp?Type=Flash") %>";
FCKConfig.FlashBrowserWindowWidth  = 700;
FCKConfig.FlashBrowserWindowHeight = 500;