<%@page import="java.io.UnsupportedEncodingException,
	org.opencms.i18n.CmsEncoder,
	org.opencms.editors.tinymce.*,
	org.opencms.util.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	java.util.*
"%><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%

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
grp.append(",searchreplace");

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}


//Group
grp = new StringBuilder() ;

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
grp.append(",cut,copy,paste,pastetext");

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
	if (options.showElement("option.link", displayOptions)||options.showElement("option.extlink", displayOptions)) {
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

if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;



if (options.showElement("option.specialchars", displayOptions)) {
	grp.append(",charmap");
}

if (options.showElement("option.spellcheck", displayOptions)) {
	grp.append(",spellchecker");
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
		grp.append(",subscript");
	}
	if (options.showElement("button.super", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",superscript");
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
		grp.append(",alignleft");
	}
	if (options.showElement("button.aligncenter", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",aligncenter");
	}
	if (options.showElement("button.alignright", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",alignright");
	}
	if (options.showElement("button.justify", CmsStringUtil.TRUE, displayOptions)) {
		grp.append(",alignjustify");
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

if (options.showElement("button.hr", CmsStringUtil.TRUE, displayOptions)) {
	grp.append(",hr");
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


//determine if the help button should be shown
if (wp.isHelpEnabled()) {
	if (options.showElement("option.help", displayOptions)) {
		grp.append(",oc-help");
	}
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




if(grp.length() > 0){
	toolbar.append(grp.toString() + "," + CmsTinyMCE.GROUP_SEPARATOR);
}

//Group
grp = new StringBuilder() ;


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
	//$FALL-THROUGH$
case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content
	if (wp.getAction() == CmsEditor.ACTION_SAVE) {
		wp.actionSave();
	}
	//$FALL-THROUGH$
case CmsDialog.ACTION_DEFAULT:
case CmsEditor.ACTION_SHOW:
default:
//////////////////// ACTION: show editor frame (default)

	// escape the content parameter to display it in the form
	wp.escapeParams();
	wp.setParamAction(null);

%><!DOCTYPE html>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel="stylesheet" type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">
<link rel="stylesheet" type="text/css" href="<%= cms.link("/system/workplace/editors/tinymce/tinymce_xmlpage.css") %>">
<script type="text/javascript" src="<%= CmsWorkplace.getStaticResourceUri("editors/tinymce/jscripts/tinymce/tinymce.min.js") %>"></script>
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
	var isWp = false;
    try { 
        if (top.document.querySelector(".o-editor-frame")) {
            isWp = true; 
        } else { 
            isWp = false; 
        }
    } catch (e) {}
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
var toolbarButtons="<%= CmsTinyMCE.buildToolbar(toolbar.toString())%>";
var contextmenu="";
if (toolbarButtons.indexOf("link")>0)
    contextmenu+="link";
if (toolbarButtons.indexOf("OcmsDownloadGallery")>0)
    contextmenu+=" OcmsDownloadGallery";
if (toolbarButtons.indexOf("OcmsImageGallery")>0)
    contextmenu+=" OcmsImageGallery";
if (toolbarButtons.indexOf("table")>0)
    contextmenu+=" inserttable | cell row column deletetable"

var plugins = "anchor,charmap,codemirror,importcss,textcolor,autolink,lists,pagebreak,table,save,hr,image,link,emoticons,insertdatetime,preview,media,searchreplace,print,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,template,wordcount,advlist,-opencms";


tinyMCE.init({
    // General options
    codemirror: {
      indentOnInit: true, // whether or not to indent code on init.
      path: "<%= CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getStaticResourceContext() , "editors/codemirror/dist/") %>", // path to CodeMirror distribution
      config: {           // CodeMirror config object
         lineNumbers: true
      }
    },
    toolbar_items_size: 'small',
    mode : "exact",
    elements : "tinymce_content",
    theme : "silver",
    plugins : plugins,
    importcss_append: true,
    contextmenu: contextmenu,
    file_picker_callback : cmsTinyMceFileBrowser,
	toolbar: toolbarButtons,
	toolbar_items_size: 'small',
    menubar:false,
    resize : false,
    entity_encoding: "named",
    entities: '160,nbsp',
    paste_as_text: <%=""+Boolean.valueOf(OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getEditorConfiguration("tinymce").getParameters().get("paste_text"))%>,
    cmsGalleryEnhancedOptions : <%= options.showElement("gallery.enhancedoptions", displayOptions)%>,
    cmsGalleryUseThickbox : <%= options.showElement("gallery.usethickbox", displayOptions)%>,
    language : "<%= wp.getLocale().getLanguage() %>",
	relative_urls: false,
    remove_script_host: false,

    // Example content CSS (should be your site CSS)
    content_css : "<cms:link><%= wp.getUriStyleSheet() %></cms:link>",
    
    // editor size
    width: "100%",
    height: "100%",
    valid_children : "+body[style]",
    //element options
    valid_elements: "*[*]",
    allow_script_urls: true,
    <%
    if(formatSelectOption){
    	String format = options.getOptionValue("formatselect.options", "", displayOptions);
    	if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(format)){
	    	String[] formats=format.split(";");
	    	format="";
	    	for (int i=0; i < formats.length; i++){
	    	    format+=formats[i].toUpperCase()+"="+formats[i];
	    	    if (i<formats.length-1){
	    	        format+=";";
	    	    }
	    	}
	    	%>
	    	block_formats : "<%=format%>",
	    	<%
    	}
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
		  ed.on("init",function(event) {
		      event.target.setContent(decodeURIComponent('<%= wp.getParamContent() %>'));
		      event.target.undoManager.clear();
		      addCustomShortcuts(event.target);
		  });
		//  setupTinyMCE(ed);
		// add icons
		ed.ui.registry.addIcon('publish','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 32 32"><title>publish2</title><path fill="#444" d="M15.958 2c-5.355 0-9.777 3.034-12.077 7.558l-2.881-1.289 0.030 11.267 9.349-7.070-2.734-1.223c1.659-3.082 4.613-5.243 8.313-5.243 5.389 0 10.042 4.532 10.042 10 0 5.466-4.652 10-10.042 10-3.364 0-6.084-1.851-7.844-4.481l-3.035 2.827c2.527 3.425 6.347 5.655 10.879 5.655 7.635 0 14.042-6.256 14.042-14 0-7.745-6.407-14-14.042-14zM16 9c-0.54 0-1.004 0.452-1 1l0.043 5.93c0.001 0.129 0.036 0.48 0.119 0.703 0.087 0.235 0.298 0.555 0.388 0.645l3.656 3.537c0.381 0.386 0.988 0.481 1.37 0.095 0.381-0.388 0.349-1.005-0.033-1.394l-3.544-3.516v-6c0-0.546-0.46-1-1-1z"></path></svg>');
      ed.ui.registry.addIcon('save','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"  viewBox="0 0 32 32"><title>save</title><path fill="#444" d="M15.087 23.078h-3.987l-0.032 4.902 3.987 0.032zM3.005 27.985c-0.005 2.015 0.995 3.015 3.015 3.057l17.994 0.032 5.018-5.040-0-22.003c-0.032-2.031-1.032-3.031-2.993-3.031h-20.030c-2.008 0-3.040 1-2.987 2.999 0 0-0.031 16.691-0.016 23.986zM21.012 29.038h-10.975l-0.054-7.021 10.965-0.032zM26.036 15.977h-20.017v-11.932l19.985-0.032z"></path></svg>');
      ed.ui.registry.addIcon('save-exit','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 32 32"><title>save-exit</title><path fill="#444" d="M24 0c-1.405 0-2.725 0.364-3.873 1h-14.119c-2.008 0-3.040 1-2.988 2.999 0 0-0.031 16.691-0.016 23.986v0c-0.005 2.015 0.995 3.015 3.015 3.057l17.994 0.032 5.018-5.040 0.032-11.841c1.792-1.467 2.937-3.697 2.937-6.193 0-4.417-3.582-8-8-8zM24 1.079c3.823 0 6.923 3.099 6.923 6.922s-3.101 6.922-6.923 6.922c-3.823 0-6.922-3.099-6.922-6.922s3.099-6.922 6.922-6.922zM27.001 2.956l-2.993 3.052-3.017-3.018-1.968 2.006 2.995 3.028-2.956 2.982 1.928 1.967 2.999-3.009 3.003 3.036 2-2.009-2.955-2.956 2.963-3.038-1.999-2.042zM17.083 3.982c-0.688 1.181-1.083 2.553-1.083 4.018 0 4.226 3.277 7.683 7.427 7.977h-17.409v-11.932l11.065-0.063zM20.948 21.985l0.064 7.053h-10.975l-0.054-7.021 10.965-0.032zM11.1 23.078l-0.032 4.902 3.987 0.032 0.032-4.934h-3.987z"></path></svg>');
      ed.ui.registry.addIcon('exit','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 32 32"><title>exit</title><path fill="#444" d="M16 2c-7.732 0-14 6.268-14 14s6.268 14 14 14c7.732 0 14-6.268 14-14s-6.268-14-14-14zM25.025 22.027l-2.965 3.062-6.010-6.060-6.063 5.98-2.997-2.935 5.994-6.038-5.994-6.042 2.997-3.030 6.042 6.028 5.999-6.012 3.029 3.030-6.057 6.007z"></path></svg>');
		
		// Add Publisg button
	    ed.ui.registry.addButton('oc-publish', {
	    	tooltip : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_EXPLORER_CONTEXT_PUBLISH_0), encoding)  %>',
	        icon: 'publish',
	        onAction : function() {
	        	var exitTarget='_top';
	        	//the editors exit frame target, may be !='_top' if in advanced direct edit!
	        	if (top.frames['cmsAdvancedDirectEditor']!=null && top.frames['cmsAdvancedDirectEditor'].document!=null){
	        	    exitTarget='cmsAdvancedDirectEditor';
	        	}
	        	execAction(tinyMCE.get('tinymce_content'), '<%= CmsEditor.EDITOR_SAVEACTION %>',exitTarget);
	        }
	   });
		
	 	// Add Save & Exit button
	    ed.ui.registry.addButton('oc-save-exit', {
	    	tooltip : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0), encoding) %>',
	        icon: 'save-exit',
	        onAction : ocmsSaveExit
	   });
	   
	 	// Add Save button
	    ed.ui.registry.addButton('oc-save', {
	    	tooltip : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0), encoding) %>',
	        icon: 'save',
	        onAction : ocmsSave
	   });

	 	// Add Exit button
	    ed.ui.registry.addButton('oc-exit', {
	    	tooltip : '<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0), encoding) %>',
	        icon: 'exit',
	        onAction : ocmsExit
	   });
	}
});

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
    var isWp = false;
    try { 
        if (top.document.querySelector(".o-editor-frame")) {
            isWp = true; 
        } else { 
            isWp = false; 
        }
    } catch (e) {}
	var form = document.forms["EDITOR"];
	form.content.value = encodeURIComponent(editor.getContent());
	form.action.value = action;
	form.target = target;
	  if (isWp) {
         form.target="_self";
      }
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

<form style="position:fixed; top:0; left:0; right:0; bottom:0; margin:0px; padding:0px;" name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
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

<table cellspacing="0" cellpadding="0" border="0" style="width:100%; height:100%; table-layout:fixed;">

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
<div id="textarea-container" class="cmsTinyMCE" style="width:100%; height:100%; background-color: /*begin-color Window*/#ffffff/*end-color*/;">
<script language="javascript">
document.write ('<textarea id="tinymce_content" name="tinymce_content" style="height:'+getEditorHeight()+'px; width:100%;"></textarea>');
</script>
</div>
</td>
</tr>

</table>

</form>

<form style="display: none;" name="ELEMENTS" action="<%= cms.link("/system/workplace/editors/dialogs/elements.jsp") %>" target="DIALOGELEMENT" method="post">
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
