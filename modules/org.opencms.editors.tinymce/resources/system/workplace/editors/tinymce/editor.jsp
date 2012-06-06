<%@page import="java.io.UnsupportedEncodingException"%>
<%@ page taglibs="cms" import="
	org.opencms.i18n.CmsEncoder,
	org.opencms.editors.tinymce.*,
	org.opencms.util.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	org.apache.commons.lang.StringUtils,
	java.util.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsTinyMCE wp = new CmsTinyMCE(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);
String encoding = CmsEncoder.ENCODING_US_ASCII;

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

////////////////////start of building toolbar
String resource = wp.getParamResource();
StringBuilder toolbar = new StringBuilder();
StringBuilder grp ;

grp = new StringBuilder();
grp.append("oc-exit");
toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);

// Group
grp = new StringBuilder() ;
if (CmsStringUtil.isNotEmpty(resource) && options.showElement("button.customized", displayOptions)) {
	I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
	if (actionClass.isButtonActive(wp.getJsp(), resource)) {
		grp.append(",oc-publish,");
	}
}

grp.append("oc-save-exit,oc-save");
toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);

//Group
grp = new StringBuilder() ;
if (options.showElement("option.sourcecode", displayOptions)) {
	grp.append(",code");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

grp.append(",undo,redo");

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;
grp.append(",search,replace");

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}


//Group
grp = new StringBuilder() ;

if (options.showElement("button.hr", displayOptions)) {
	grp.append(",hr");
}

grp.append(",selectall") ;

if (options.showElement("button.removeformat", displayOptions)) {
	grp.append(",removeformat");
}

if (options.showElement("button.visualaid", displayOptions)) {
	grp.append(",visualaid");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;
grp.append(",cut,copy,paste,pastetext,pasteword");

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine if the insert table button should be shown
if (options.showElement("option.table", displayOptions)) {
	grp.append(",table");
}

if (options.showElement("button.media", displayOptions)) {
	grp.append(",media");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine if the insert link buttons should be shown
if (options.showElement("option.links", displayOptions)) {

	// determine if the local link button should be shown
	if (options.showElement("option.link", displayOptions)) {
		grp.append(",oc-link");
	}

	// determine if the external link button should be shown
	if (options.showElement("option.extlink", displayOptions)) {
		grp.append(",link");
	}

	// determine if the anchor button should be shown
	if (options.showElement("option.anchor", displayOptions)) {
		grp.append(",anchor");
	}

	// determine if the unlink buttons should be shown
	if (options.showElement("option.unlink", displayOptions)) {
		grp.append(",unlink");
	}

}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine if the insert/edit image button should be shown
if (options.showElement("option.images", displayOptions) || options.showElement("gallery.image", displayOptions)) {
	// replaced by image gallery: toolbar.append(",'-', 'OcmsImage'");
	grp.append(",OcmsImageGallery");
}

if (options.showElement("gallery.download", displayOptions)) {
	grp.append(",OcmsDownloadGallery");
}

if (options.showElement("gallery.link", displayOptions)) {
	grp.append(",OcmsLinkGallery");
}

if (options.showElement("gallery.html", displayOptions)) {
	grp.append(",OcmsHtmlGallery");
}

if (options.showElement("gallery.table", displayOptions)) {
	grp.append(",OcmsTableGallery");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.advhr", displayOptions)) {
	grp.append(",advhr");
}


if (options.showElement("option.specialchars", displayOptions)) {
	grp.append(",charmap");
}

if (options.showElement("option.spellcheck", displayOptions)) {
	grp.append(",iespell");
}



if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}


//Group
grp = new StringBuilder() ;

//determine if the print button should be shown
if (options.showElement("option.print", displayOptions)) {
	grp.append(",print");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//style buttons
String cssPath = wp.getUriStyleSheet() ;
String styleFile = null;
boolean stylePresent = false;
if (CmsStringUtil.isNotEmpty(cssPath)) {
	String pathUsed = cssPath;
	int idx = pathUsed.indexOf('?');
	if (idx != -1) {
		pathUsed = cssPath.substring(0, idx);
	}
	styleFile = pathUsed + CmsTinyMCE.SUFFIX_STYLE;
	if (cms.getCmsObject().existsResource(styleFile)) {
		stylePresent = true;
	}
}
boolean style = stylePresent && options.showElement("option.style", displayOptions);
boolean formatSelectOption = false ;
//determine if the font format selector should be shown
if (options.showElement("option.formatselect", CmsStringUtil.TRUE, displayOptions)) {
	grp.append(",formatselect");
	formatSelectOption = true ;
}
//determine if the font face selector should be shown
if (options.showElement("font.face", displayOptions)) {
	grp.append(",fontselect");
}

//determine if the font size selector should be shown
if (options.showElement("font.size", displayOptions)) {
	grp.append(",fontsizeselect");
}

//determine if the style selector should be shown
if (style) {
	grp.append(",styleselect");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}











//Group
grp = new StringBuilder() ;

//determine if the font decoration buttons should be shown
if (options.showElement("font.decoration", displayOptions)) {
	if (options.showElement("button.bold", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",bold");
	}
	if (options.showElement("button.italic", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",italic");
	}
	if (options.showElement("button.underline", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",underline");
	}
	if (options.showElement("button.strikethrough", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",strikethrough");
	}

	if (options.showElement("button.sub", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",sub");
	}
	if (options.showElement("button.super", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",sup");
	}
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine if the text alignment buttons should be shown
if (options.showElement("text.align", displayOptions)) {
	if (options.showElement("button.alignleft", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",justifyleft");
	}
	if (options.showElement("button.aligncenter", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",justifycenter");
	}
	if (options.showElement("button.alignright", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",justifyright");
	}
	if (options.showElement("button.justify", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",justifyfull");
	}
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}





//Group
grp = new StringBuilder() ;

//determine if the text list buttons should be shown
if (options.showElement("text.lists", displayOptions)) {
	if (options.showElement("button.orderedlist", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",numlist");
	}
	if (options.showElement("button.unorderedlist", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",bullist");
	}
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine if the text indentation buttons should be shown
if (options.showElement("text.indent", displayOptions)) {
	if (options.showElement("button.outdent", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",outdent");
	}
	if (options.showElement("button.indent", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",indent");
	}
}

if (options.showElement("button.blockquote", CmsStringUtil.TRUE, displayOptions)) {
	grp.append(",blockquote");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

//determine which color selectors should be shown
if (options.showElement("font.color", displayOptions)) {
	grp.append(",forecolor");
}

if (options.showElement("bg.color", displayOptions)) {
	grp.append(",backcolor");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}



//Group
grp = new StringBuilder() ;

if (options.showElement("button.image", displayOptions)) {
	grp.append(",image");
}

if (options.showElement("option.cleanup", displayOptions)) {
	grp.append(",cleanup");
}

//determine if the help button should be shown
if (wp.isHelpEnabled()) {
	if (options.showElement("option.help", displayOptions)) {
		grp.append(",oc-help");
	}
}

if (options.showElement("option.sourcecode", displayOptions)) {
	grp.append(",code");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.insertdate", displayOptions)) {
	grp.append(",insertdate");
}

if (options.showElement("button.inserttime", displayOptions)) {
	grp.append(",inserttime");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}





//Group
grp = new StringBuilder() ;

if (options.showElement("button.ltr", displayOptions)) {
	grp.append(",ltr");
}

if (options.showElement("button.rtl", displayOptions)) {
	grp.append(",rtl");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.insertlayer", displayOptions)) {
	grp.append(",insertlayer");
}

if (options.showElement("button.moveforward", displayOptions)) {
	grp.append(",moveforward");
}

if (options.showElement("button.movebackward", displayOptions)) {
	grp.append(",movebackward");
}

if (options.showElement("button.absolute", displayOptions)) {
	grp.append(",absolute");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.styleprops", displayOptions)) {
	grp.append(",styleprops");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.cite", displayOptions)) {
	grp.append(",cite");
}

if (options.showElement("button.abbr", displayOptions)) {
	grp.append(",abbr");
}

if (options.showElement("button.acronym", displayOptions)) {
	grp.append(",acronym");
}

if (options.showElement("button.del", displayOptions)) {
	grp.append(",del");
}

if (options.showElement("button.ins", displayOptions)) {
	grp.append(",ins");
}

if (options.showElement("button.attribs", displayOptions)) {
	grp.append(",attribs");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;

if (options.showElement("button.visualchars", displayOptions)) {
	grp.append(",visualchars");
}

if (options.showElement("button.nonbreaking", displayOptions)) {
	grp.append(",nonbreaking");
}

if (options.showElement("button.template", displayOptions)) {
	grp.append(",template");
}

if (options.showElement("button.pagebreak", displayOptions)) {
	grp.append(",pagebreak");
}

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//////////////////// start of switch statement

switch (wp.getAction()) {

case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog

	// do nothing here, only prevents editor content from being displayed!

break;
case CmsEditor.ACTION_PREVIEW:
//////////////////// ACTION: preview the page

	wp.actionPreview();

break;
case CmsEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor

	wp.actionExit();

break;
case CmsEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;

case CmsEditor.ACTION_DELETELOCALE:
//////////////////// ACTION: delete a localeand show the editor again
	if (wp.getAction() == CmsEditor.ACTION_DELETELOCALE) {
		wp.actionDeleteElementLocale();
    }

case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content
	if (wp.getAction() == CmsEditor.ACTION_SAVE) {
		wp.actionSave();
	}

case CmsDialog.ACTION_DEFAULT:
case CmsEditor.ACTION_SHOW:
default:
//////////////////// ACTION: show editor frame (default)

	// escape the content parameter to display it in the form
	wp.escapeParams();
	wp.setParamAction(null);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">
<link rel=stylesheet type="text/css" href="<%= cms.link("tinymce_xmlpage.css") %>">
<!-- <script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/gallery.js</cms:link>"></script>
<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/link.js</cms:link>"></script>-->
<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() + "editors/tinymce/jscripts/tiny_mce/" %>tiny_mce_src.js"></script>
<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() + "jquery/packed/" %>jquery.js"></script>
<script type="text/javascript">

// dialog windows
var dialogCharWindow = null;
var dialogElementWindow = null;
var dialogAnchorWindow = null;
var dialogPropertyWindow = null;

// OpenCms context prefix, required for page editor because no link replacement is done here
var linkEditorPrefix = "<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";

// Path to the style sheet used in the editor
var cssPath = "<%= wp.getUriStyleSheet() %>";

// Path to the currently edited resource
var editedResource = "<%= wp.getParamResource() %>";

// saves the editors contents
function saveContent() {
    document.EDITOR.content.value = encodeURIComponent(tinyMCE.get('tinymce_content').getContent());
}

// Ask user whether he really wants to delete the locale
function confirmDeleteLocale() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_DELETELOCALE_0) %>")) {
		buttonAction(14);
	}
}

// action on button click
function buttonAction(para) {
	var _form = document.EDITOR;
	_form.action.value = "";
    switch (para) {
    case 1:
        // reload the editor
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_SHOW %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 2:
        // preview selected
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_PREVIEW %>";
        _form.target = "PREVIEW";
        _form.submit();
        break;
    case 3:
        // change element
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_CHANGE_ELEMENT %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 4:
        // open elements window
    	saveContent();
        dialogElementWindow = window.open("about:blank","DIALOGELEMENT","width=320,height=250,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
        document.ELEMENTS.submit();
        dialogElementWindow.focus();
        break;
    case 5:
        // open properties window
    	saveContent();
        dialogPropertyWindow = window.open("about:blank","DIALOGPROPERTY","width=600,height=280,left=0,top=0,resizable=yes,scrollbars=yes,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
        document.PROPERTIES.submit();
        dialogPropertyWindow.focus();
        break;
    case 13:
        // clear document
        saveContent();
		_form.action.value = "<%= CmsEditor.EDITOR_CLEANUP %>";
		_form.target = "_self";
		_form.submit();
        break;
	case 14:
		// delete the current locale content
		_form.action.value = "<%= CmsEditor.EDITOR_DELETELOCALE %>";
		_form.submit();
		break;
    }
}

function changeElement(elementName, language) {
    if (elementName != document.EDITOR.<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>.value && language == document.EDITOR.<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>.value) {
        document.EDITOR.<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>.value = elementName;
        buttonAction(3);
    } else {
        buttonAction(1);
    }
}

// action for closing the window
function closeDialog() {
    if (dialogElementWindow) {
        window.dialogElementWindow.close();
    }
    if (dialogPropertyWindow) {
        window.dialogPropertyWindow.close();
    }
    if (dialogCharWindow) {
    	window.dialogCharWindow.close();
    }
    if (dialogAnchorWindow) {
    	window.dialogAnchorWindow.close();
    }

    var actionValue = document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value;
    if (actionValue == null || actionValue == "null" || actionValue == "") {
		closeBrowserWindow();
    }

}

// sends a request to the server to delete the temporary file if the browser was accidentally closed
function closeBrowserWindow() {
   var http_request = false;
   if (window.XMLHttpRequest) { // Mozilla, Safari, ...
      http_request = new XMLHttpRequest();
      if (http_request.overrideMimeType) {
         http_request.overrideMimeType('text/xml');
      }
   } else if (window.ActiveXObject) { // IE
      try {
         http_request = new ActiveXObject("Msxml2.XMLHTTP");
      } catch (e) {
         try {
            http_request = new ActiveXObject("Microsoft.XMLHTTP");
         } catch (e) {}
      }
   }
   if (!http_request) {
	return false;
   }
   http_request.onreadystatechange = httpStateDummy;
   http_request.open("POST", "<%= wp.getDialogRealUri() %>?<%= CmsDialog.PARAM_ACTION %>=<%= CmsEditor.EDITOR_CLOSEBROWSER %>&<%= CmsDialog.PARAM_RESOURCE %>=<%= wp.getParamResource() %>&<%= CmsEditor.PARAM_TEMPFILE %>=<%= wp.getParamTempfile() %>", true);
   http_request.send('');
}

// dummy function for html request
function httpStateDummy() {
	return;
}

// action when closing a popup window
function popupCloseAction(closeObj) {
	if (closeObj.refreshOpener && closeObj.refreshOpener == true) {
		buttonAction(1);
	} else if (closeObj.elemName) {
		changeElement(closeObj.elemName, closeObj.elemLocale);
	}
}
</script>
<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/opencms_plugin.js</cms:link>"></script>
<script type="text/javascript">
<!--
<%=CmsTinyMCEConfiguration.get(cms.getCmsObject()).generateOptionPreprocessor("cms_preprocess_options")%>
tinyMCE.init(cms_preprocess_options({
    // General options
    mode : "exact",
    elements : "tinymce_content",
    theme : "advanced",
    plugins : "autolink,lists,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,-opencms",
    file_browser_callback : 'cmsTinyMceFileBrowser',

    // Theme options
    <%= CmsTinyMCE.buildToolbar(toolbar.toString())%>
    theme_advanced_toolbar_location : "top",
    theme_advanced_toolbar_align : "left",
    theme_advanced_statusbar_location : "bottom",
    theme_advanced_resizing : false,
    cmsGalleryEnhancedOptions : <%= options.showElement("gallery.enhancedoptions", displayOptions)%>,
    cmsGalleryUseThickbox : <%= options.showElement("gallery.usethickbox", displayOptions)%>,
    language : "<%= wp.getLocale().getLanguage() %>",

    // Skin options
    skin_variant : "ocms",
    relative_urls: false,
    remove_script_host: false,

    // Example content CSS (should be your site CSS)
    content_css : "<cms:link><%= wp.getUriStyleSheet() %></cms:link>",
    
    // editor size
    width: "100%",
    height: "100%",
    valid_children : "+body[style]",
    //element options
    extended_valid_elements : "style[dir<ltr?rtl|lang|media|title|type],link[charset|class|dir<ltr?rtl|href|hreflang|id|lang|media|onclick|ondblclick|onkeydown|onkeypress|onkeyup|onmousedown|onmousemove|onmouseout|onmouseover|onmouseup|rel|rev|style|title|target|type]",
    <%
    if(formatSelectOption){
    	String format = options.getOptionValue("formatselect.options", "", displayOptions);
    	format = StringUtils.replace(format, ";", ",");
    	%>
    	theme_advanced_blockformats : "<%=format%>",
    	<%
    }
    %>
    
    <%
    if(style){
    	String styleContent ;
    	try{
    	styleContent = new String(cms.getCmsObject().readFile(styleFile).getContents(),
    			OpenCms.getSystemInfo().getDefaultEncoding());
    	} catch(UnsupportedEncodingException e){
    		styleContent = null  ;
    	}
    	
    	if(styleContent !=null){
    		%>
    		style_formats : <%= styleContent%>,
    		<%
    	}
    }
    %>
    
    // events
    setup : function(ed) {
		  ed.onInit.add(function(ed) {
		      ed.setContent(decodeURIComponent('<%= wp.getParamContent() %>'));
		      ed.undoManager.clear();
		      addCustomShortcuts(ed);
		  });
		  setupTinyMCE(ed);
		// Add Publisg button
	    ed.addButton('oc-publish', {
	    	title : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_EXPLORER_CONTEXT_PUBLISH_0), encoding)  %>',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-publish.gif")%>',
	        onclick : function() {
	        	var exitTarget='_top';
	        	//the editors exit frame target, may be !='_top' if in advanced direct edit!
	        	if (top.frames['cmsAdvancedDirectEditor']!=null && top.frames['cmsAdvancedDirectEditor'].document!=null){
	        	    exitTarget='cmsAdvancedDirectEditor';
	        	}
	        	execAction(tinyMCE.get('tinymce_content'), '<%= CmsEditor.EDITOR_SAVEACTION %>',exitTarget);
	        }
	   });
		
	 	// Add Save & Exit button
	    ed.addButton('oc-save-exit', {
	    	title : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0), encoding) %>',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-save-exit.gif")%>',
	        onclick : ocmsSaveExit
	   });
	   
	 	// Add Save button
	    ed.addButton('oc-save', {
	    	title : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0), encoding) %>',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-save.gif")%>',
	        onclick : ocmsSave
	   });

	 	// Add Exit button
	    ed.addButton('oc-exit', {
	    	title : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0), encoding) %>',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-exit.gif")%>',
	        onclick : ocmsExit
	   });
	}
}));

function setupTinyMCE(editor) {
   if (tinyMCE.isWebKit) {
      // fix weird layout problem in Chrome 
      // If we don't do this, the button bar won't wrap if the window is too small 
      editor.onInit.add(function() {
         var id = editor.id + "_tbl";
         var baseElem = document.getElementById(id); 
         var modElem = $(baseElem).parents(".cmsTinyMCE").get(0);
         $(modElem).removeClass("cmsTinyMCE");
         window.setTimeout(function() { $(modElem).addClass("cmsTinyMCE"); } , 1);
      });
   }
}

function ocmsSaveExit() {
	execAction(tinyMCE.get('tinymce_content'), '<%= CmsEditor.EDITOR_SAVEEXIT %>','_top');
}

function ocmsSave() {
	execAction(tinyMCE.get('tinymce_content'), '<%= CmsEditor.EDITOR_SAVE %>','_self');
}

function ocmsExit() {
	if (!tinyMCE.get('tinymce_content').isDirty() || confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
		execAction(tinyMCE.get('tinymce_content'), '<%= CmsEditor.EDITOR_EXIT %>','_top');
	}
}

function addCustomShortcuts(editor){
	editor.addShortcut('ctrl+s','',ocmsSave);
	editor.addShortcut('ctrl+shift+x','',ocmsExit);
	editor.addShortcut('ctrl+shift+s','',ocmsSaveExit);
	editor.addShortcut('ctrl+shift+z','','Redo');
	editor.addShortcut('ctrl+l','','mceAdvLink');
}

//sets field values and submits the editor form
function execAction(editor, action, target) {
	var form = document.forms["EDITOR"];
	form.content.value = encodeURIComponent(editor.getContent());
	form.action.value = action;
	form.target = target;
	form.submit(); 
}
// JavaScript resize editor stuff

// height of bottom line of editor with HTML tag information
var tagBarHeight = 14;

// calculate editor height in pixels
function getEditorHeight(){
	return document.getElementById('textarea-container').clientHeight -tagBarHeight;
}

//-->
</script>
</head>

<body class="buttons-head" unselectable="on" onunload="closeDialog();">

<form style="width:100%; height:100%; margin:0px; padding:0px; " name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_OLDELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
<input type="hidden" name="content" id="content" >

<table cellspacing="0" cellpadding="0" border="0" style="width:100%; height:100%;">

<tr><td>
<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%
boolean elementSelection = options.showElement("option.element.selection", displayOptions);
boolean elementLanguage = options.showElement("option.element.language", displayOptions);
if (elementSelection || elementLanguage) {
	out.println(wp.buttonBarLabel(org.opencms.workplace.editors.Messages.GUI_INPUT_ELEMENT_0));
	if (elementLanguage) {
		out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + CmsEditor.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.deleteLocaleButton("javascript:confirmDeleteLocale();", null, "deletelocale", org.opencms.workplace.editors.Messages.GUI_BUTTON_DELETE_0, buttonStyle));
		out.println(wp.buttonBarSpacer(2));
	} else {
		%><input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><%
	}
	if (elementSelection) {
		out.println("<td>" + wp.buildSelectElementName("name=\"" + CmsDefaultPageEditor.PARAM_ELEMENTNAME + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.buttonBarSeparator(5, 5));
		out.println(wp.button("javascript:buttonAction(4);", null, "elements", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_ELEMENTS_BUTTON_0, buttonStyle));
	} else {
		%><input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
	}
} else {
	// build hidden input fields that editor works correctly
	%><input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
}
if (options.showElement("option.properties", displayOptions)) {
	if (elementLanguage && !elementSelection) {
		out.println(wp.buttonBarSeparator(5, 5));
	}
	out.println(wp.button("javascript:buttonAction(5);", null, "properties", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_PROPERTIES_BUTTON_0, buttonStyle));
}
if (options.showElement("option.cleanup", displayOptions)) {
	out.println(wp.button("javascript:buttonAction(13);", null, "cleanup", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_CLEANUP_BUTTON_0, buttonStyle));
}
%>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:buttonAction(2);", null, "preview", org.opencms.workplace.editors.Messages.GUI_BUTTON_PREVIEW_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</td></tr>

<tr>
<td style="width:100%; height:100%;">
<div id="textarea-container" class="cmsTinyMCE" style="width:100%; height:100%; background-color: Window;">
<script language="javascript">
document.write ('<textarea id="tinymce_content" name="tinymce_content" style="height:'+getEditorHeight()+'px; width:100%;"></textarea>');
</script>
</div>
</td>
</tr>

</table>

</form>

<form style="display: none;" name="ELEMENTS" action="dialogs/elements.jsp" target="DIALOGELEMENT" method="post">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="ispopup" value="true">
</form>

<form style="display: none;" name="PROPERTIES" action="<%= cms.link("/system/workplace/commons/property.jsp") %>" target="DIALOGPROPERTY" method="post">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="usetempfileproject" value="true">
<input type="hidden" name="<%= CmsDialog.PARAM_ISPOPUP %>" value="true">
</form>

</body>
</html><%
}
%>
