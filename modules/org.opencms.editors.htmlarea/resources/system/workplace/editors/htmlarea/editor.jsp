<%@ page import="
	org.opencms.editors.htmlarea.*,
	org.opencms.widgets.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	java.util.*"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsHtmlAreaEditor wp = new CmsHtmlAreaEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

boolean showTableOptions = options.showElement("option.table", displayOptions);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsSimplePageEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	
	// do nothing here, only prevents editor content from being displayed!
	
break;
case CmsSimplePageEditor.ACTION_PREVIEW:
//////////////////// ACTION: preview the page

	wp.actionPreview();

break;
case CmsSimplePageEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor

	wp.actionExit();

break;
case CmsSimplePageEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;
case CmsSimplePageEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content

	wp.actionSave();

case CmsSimplePageEditor.ACTION_DEFAULT:
case CmsSimplePageEditor.ACTION_SHOW:
default:
//////////////////// ACTION: show editor frame (default)

	// escape the content parameter to display it in the form
	wp.escapeParams();

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>htmlarea-ocms.js"></script>
<script type="text/javascript">
// the content as escaped string
var __content = "<%= wp.getParamContent() %>";

// OpenCms workplace path
var workplacePath="<%= cms.link("/system/workplace/") %>";

// dialog windows
var dialogElementWindow = null;
var dialogPropertyWindow = null;

// display settings (disable style inputs for popups in HTMLArea, does not work properly!)
// var USE_LINKSTYLEINPUTS = <%= options.showElement("linkstyleinputs", displayOptions) %>;
USE_LINKSTYLEINPUTS = false;

// OpenCms context prefix, required for page editor because no link replacement is done here
var linkEditorPrefix = "<%= org.opencms.main.OpenCms.getSystemInfo().getOpenCmsContext() %>";

// inits the editors contents
function initContent() {
    document.EDITOR.edit1.value = decodeURIComponent(__content);
}

// saves the editors contents
function saveContent() {
    document.EDITOR.content.value = encodeURIComponent(getActiveEditor().getHTML());
}

//action on button click 
function buttonAction(para) {
	var _form = document.EDITOR;
	_form.action.value = "";
    switch (para) {
    case 1:
        // reload the editor
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_SHOW %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 2:
        // preview selected 
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_PREVIEW %>";
        _form.target = "PREVIEW";
        _form.submit();
        break;
    case 3:
        // change element
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_CHANGE_ELEMENT %>";
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
        dialogPropertyWindow = window.open("about:blank","DIALOGPROPERTY","width=600,height=280,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
        document.PROPERTIES.submit();
        dialogPropertyWindow.focus();
        break;
    case 6:
    	// exit without saving 
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_EXIT %>";
        _form.target = "_top";
        _form.submit();
        break;
    case 7:
    	// save and exit
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_SAVEEXIT %>";
        _form.target = "_top";
        _form.submit();
        break;
    case 8:
    	// save
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_SAVE %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 9:
    	// save and reload top editor frame
    	saveContent();
        _form.action.value = "<%= wp.EDITOR_SAVEACTION %>";
        _form.target = "_top";
        _form.submit();
        break;
    case 10:
    	// open the special character dialog popup
    	dialogCharWindow = window.open("dialogs/specialchars.jsp", "characters", "width=450, height=300, resizable=no, scrollbars=no, location=no, menubar=no, toolbar=no,dependent=yes, top=300, left=250");
    	break;
     case 11:
    	// open the anchor dialog popup
    	openAnchorDialog("<%= wp.key("editor.message.noselection") %>");
    	break;
    	case 12:
    	// open the link dialog popup
    	openLinkDialog("<%= wp.key("editor.message.noselection") %>");
    	break;
    	case 13:
        // clear document
        saveContent();
		_form.action.value = "<%= wp.EDITOR_CLEANUP %>";
		_form.target = "_self";
		_form.submit();
        break;		
    case 30:
		openOnlineHelp("/system/modules/org.opencms.editors.htmlarea/locales/<%= wp.getLocale() %>/help/index.html");
		break;		
    }
}

function deleteEditorContent(elementName, language) {
    if (elementName == document.EDITOR.<%= wp.PARAM_ELEMENTNAME %>.value && language == document.EDITOR.<%= wp.PARAM_ELEMENTLANGUAGE %>.value) {
        document.EDITOR.edit1.value = "";
    }
}

function changeElement(elementName, language) {
    if (elementName != document.EDITOR.<%= wp.PARAM_ELEMENTNAME %>.value && language == document.EDITOR.<%= wp.PARAM_ELEMENTLANGUAGE %>.value) {
        document.EDITOR.<%= wp.PARAM_ELEMENTNAME %>.value = elementName;
        buttonAction(3);   
    } else {
        buttonAction(1);
    }
}

function opensmallwin(url, name, w, h) {
    encodedurl = encodeURI(url);
    smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
    if (smallwindow != null) {
        if (smallwindow.opener == null) {
            smallwindow.opener = self;
        }
    }
    return smallwindow;
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
    if (document.EDITOR.<%= wp.PARAM_ACTION %>.value == "null" || document.EDITOR.<%= wp.PARAM_ACTION %>.value == "") {
        top.closeframe.closePage("<%= wp.getParamTempfile() %>", "<%= wp.getParamResource() %>");
    }        
}

// action when closing a popup window
function popupCloseAction(closeObj) {
	if (closeObj.refreshOpener && closeObj.refreshOpener == true) {
		buttonAction(1);
	} else if (closeObj.elemName) {
		changeElement(closeObj.elemName, closeObj.elemLocale);
	}
}

// confirm exit dialog
function confirmExit() {
	if (confirm("<%= wp.key("editor.message.exit") %>")) {
		buttonAction(6);
	}
}
</script>

<script type="text/javascript">
   _editor_url = "<%= wp.getSkinUri() + "editors/htmlarea/" %>";
   _editor_lang = "<%= CmsHtmlAreaWidget.getHtmlAreaLocale(cms.getCmsObject(), wp.getLocale()) %>";
</script>

<script type="text/javascript" src="<%= wp.getSkinUri() + "editors/htmlarea/" %>htmlarea.js"></script>

<script type="text/javascript">

// load the HTMLArea plugin files
<%
	if (showTableOptions) {
		%>HTMLArea.loadPlugin("TableOperations");<%
	}
%>

HTMLArea.loadPlugin("ContextMenu");
// HTMLArea.loadPlugin("CSS");

function initEditor() {
	// initialize the editor content
	initContent();
	
	// create the edior
	var __editor = new HTMLArea("edit1");
	
	// register the TableOperations plugin with the editor	
	<%
	if (showTableOptions) {
		%>__editor.registerPlugin(TableOperations);<%
	}
	%>
	__editor.registerPlugin(ContextMenu);
	// __editor.registerPlugin(CSS);


var config = __editor.config;

config.registerButton("oc-exit", "<%= wp.key("button.close") %>", __editor.imgURL("images/opencms/exit.gif"), true, function(e) { confirmExit(); });
config.registerButton("oc-save-exit", "<%= wp.key("button.saveclose") %>", __editor.imgURL("images/opencms/save_exit.gif"), true, function(e) { buttonAction(7); });
config.registerButton("oc-save", "<%= wp.key("button.save") %>", __editor.imgURL("images/opencms/save.gif"), true, function(e) { buttonAction(8); });

config.registerButton("oc-chars", "<%= wp.key("button.specialchars") %>", __editor.imgURL("../../buttons/specialchar.png"), false, function(e) { buttonAction(10); });
config.registerButton("oc-anchor", "<%= wp.key("button.anchor") %>", __editor.imgURL("../../buttons/anchor.png"), false, function(e) { buttonAction(11); });
config.registerButton("oc-link", "<%= wp.key("button.linkto") %>", __editor.imgURL("../../buttons/link.png"), false, function(e) { buttonAction(12); });

<%= wp.buildGalleryButtons(options, buttonStyle, displayOptions) %>

<%

// determine if customized button should be shown
String ocDirectPublish = ""; 

if (options.showElement("button.customized", displayOptions)) {
	I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
	ocDirectPublish = "\"oc-direct-publish\", ";
	if (actionClass.isButtonActive(wp.getJsp(), wp.getParamResource())) {
		%>config.registerButton("oc-direct-publish", "<%= wp.key("explorer.context.publish") %>", __editor.imgURL("images/opencms/publish.gif"), true, function(e) { buttonAction(9); });<%
	} else {
		%>config.registerButton("oc-direct-publish", "<%= wp.key("explorer.context.publish") %>", __editor.imgURL("images/opencms/publish_in.gif"), true, function(e) { });<%
	
	}
}

// determine if the toggle source code button should be shown
String sourceBt = "";
if (options.showElement("option.sourcecode", displayOptions)) {
	sourceBt = ", \"separator\", \"htmlmode\"";
}

StringBuffer insertButtons = new StringBuffer(128);

// determine if the insert table button should be shown
if (showTableOptions) {
	insertButtons.append(", \"separator\", \"inserttable\"");
}
// determine if the insert link buttons should be shown
if (options.showElement("option.links", displayOptions)) {
	insertButtons.append(", \"separator\", \"oc-link\", \"oc-anchor\"");
}
// determine if the insert image button should be shown
if (options.showElement("option.images", displayOptions)) {
	insertButtons.append(", \"separator\", \"insertimage\"");
}
// determine if the image gallery button should be shown

insertButtons.append(wp.buildGalleryButtonRow(options, displayOptions));

// determine if the insert special characters button should be shown
if (options.showElement("option.specialchars", displayOptions)) {
	insertButtons.append(", \"separator\", \"oc-chars\"");
}

StringBuffer fontButtons = new StringBuffer(128);

// determine if the font face selector should be shown
if (options.showElement("font.face", displayOptions)) {
	fontButtons.append(" \"fontname\", \"space\",");
}
// determine if the font size selector should be shown
if (options.showElement("font.size", displayOptions)) {
	fontButtons.append(" \"fontsize\", \"space\",");
}

// determine if the font decoration buttons should be shown
if (options.showElement("font.decoration", displayOptions)) {
	fontButtons.append(" \"bold\", \"italic\", \"underline\", \"strikethrough\", \"separator\", \"subscript\", \"superscript\",");
}

// determine if the text alignment buttons should be shown
if (options.showElement("text.align", displayOptions)) {
	fontButtons.append(" \"separator\", \"justifyleft\", \"justifycenter\", \"justifyright\", \"justifyfull\",");
}

// determine if the text list buttons should be shown
if (options.showElement("text.lists", displayOptions)) {
	fontButtons.append(" \"separator\", \"insertunorderedlist\", \"insertorderedlist\",");
}

// determine if the text indentation buttons should be shown
String textDirection = "";
if (options.showElement("text.indent", displayOptions)) {
	fontButtons.append(" \"separator\", \"indent\", \"outdent\",");
	textDirection = ", \"separator\", \"lefttoright\", \"righttoleft\"";
}

// determine if the font color selector should be shown
String color = "";
boolean fontColor = options.showElement("font.color", displayOptions);
boolean bgColor = options.showElement("bg.color", displayOptions);
if (fontColor) {
	color = " \"forecolor\",";
}
// determine if the background color selector should be shown
if (bgColor) {
	color += " \"hilitecolor\",";
}
if (fontColor || bgColor) {
	color = " \"separator\"," + color;
}

fontButtons.append(color);
String outFontButtons = fontButtons.toString();
if (outFontButtons.endsWith(",")) {
	outFontButtons = outFontButtons.substring(0, outFontButtons.length() - 1);
}

if (showTableOptions) { %>

var tablebar = new Array();
tablebar[0] = "starttab";
for (i=0; i<config.toolbar[2].length; i++) {
	var b = config.toolbar[2][i];
	if (b != "linebreak") {
		tablebar[i+1] = b;
	}
}
<%
}

// determine help button display
String onlineHelp = "";
if (wp.isHelpEnabled()) { %>
	config.registerButton("oc-onlinehelp", "<%= wp.key("button.help") %>", __editor.imgURL("../../buttons/help.png"), true, function(e) { buttonAction(30); });<%
	onlineHelp = " \"separator\", \"oc-onlinehelp\", ";

}
%>

config.toolbar = [
	[ "starttab", <%= ocDirectPublish %> "oc-save-exit", "oc-save"<%= sourceBt %>, "separator", "undo", "redo", "separator", "cut", "copy", "paste"
	  <%= insertButtons %><%= textDirection %>, "separator", "inserthorizontalrule",<%= onlineHelp %>
	  "separator", "oc-exit" ],

	[ "starttab", "formatblock", "space",<%= outFontButtons %> ]<%
	if (showTableOptions) {
		%>,

	// table plugin will already have been added at position 2 
	tablebar<%
	}
	%>
];

	config.pageStyle = "@import url(<%= cms.link(wp.getUriStyleSheet()) %>);";
	
	__editor.generate();
	<%
	// if necessary, switch to text mode
	if ("edit".equals(wp.getParamEditormode())) {
		%>setTimeout("__editor.execCommand('htmlmode');", 500);<%
	}
	%>
	setActiveEditor(__editor);
	return false;
}

<%	if (wp.isHelpEnabled()) {
		out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale())); 
	}
%>

</script>

</head>

<body class="buttons-head" unselectable="on" onload="initEditor();" onunload="closeDialog();">

<form style="width:100%; height:100%; margin:0px; padding:0px; " name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= wp.PARAM_CONTENT %>">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= wp.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= wp.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= wp.PARAM_OLDELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= wp.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= wp.PARAM_EDITORMODE %>" value="<%= wp.getParamEditormode() %>">
<input type="hidden" name="<%= wp.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">

<table cellspacing="0" cellpadding="0" border="0" style="width:100%; height:100%;">

<tr><td>
<table cellspacing="0" cellpadding="0" border="0" style="width:100%;"><tr>
<%= wp.buttonBarStartTab(0, 0) %>
<%
boolean elementSelection = options.showElement("option.element.selection", displayOptions);
boolean elementLanguage = options.showElement("option.element.language", displayOptions);
if (elementSelection || elementLanguage) {
	out.println(wp.buttonBarLabel("input.element"));
	if (elementLanguage) {
		out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + wp.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.buttonBarSpacer(2));
	} else {
		%><input type="hidden" name="<%= wp.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><%
	}
	if (elementSelection) {
		out.println("<td>" + wp.buildSelectElementName("name=\"" + wp.PARAM_ELEMENTNAME + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.buttonBarSeparator(5, 5));
		out.println(wp.button("javascript:buttonAction(4);", null, "elements", "editor.dialog.elements.button", buttonStyle));
	} else {
		%><input type="hidden" name="<%= wp.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
	}
} else {
	// build hidden input fields that editor works correctly
	%><input type="hidden" name="<%= wp.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><input type="hidden" name="<%= wp.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
}
if (options.showElement("option.properties", displayOptions)) {
	if (elementLanguage && !elementSelection) {
		out.println(wp.buttonBarSeparator(5, 5));
	}
	out.println(wp.button("javascript:buttonAction(5);", null, "properties", "editor.dialog.properties.button", buttonStyle));
}
out.println(wp.button("javascript:buttonAction(13);", null, "cleanup", "editor.dialog.cleanup.button", buttonStyle));
%>             
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:buttonAction(2);", null, "preview", "button.preview", buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
</tr></table>
</td></tr>

<tr>
<td style="width:100%; height:100%;">
<div style="width:100%; height:100%;">
<textarea class="texteditor" name="edit1" id="edit1" style="width:100%; height:100%;"></textarea>
</div>
</td>	
</tr>

</table>

</form>

<form style="display: none;" name="ELEMENTS" action="dialogs/elements.jsp" target="DIALOGELEMENT" method="post">
<input type="hidden" name="<%= wp.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= wp.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= wp.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="ispopup" value="true">
</form>

<form style="display: none;" name="PROPERTIES" action="../commons/property.jsp" target="DIALOGPROPERTY" method="post">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="usetempfileproject" value="true">
<input type="hidden" name="<%= wp.PARAM_ISPOPUP %>" value="true">
</form>

</body>
</html>

<%
}
%>