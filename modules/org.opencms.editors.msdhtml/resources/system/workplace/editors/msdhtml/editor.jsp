<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*,
	java.util.*
"%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsMSDHtmlEditor wp = new CmsMSDHtmlEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

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
case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content

	wp.actionSave();
	if (wp.getAction() == CmsDialog.ACTION_CANCEL) {
		// an error occured during save
		break;
	}

case CmsDialog.ACTION_DEFAULT:
case CmsEditor.ACTION_SHOW:
default:
//////////////////// ACTION: show editor frame (default)

	// escape the content and title parameters to display them in a form
	wp.escapeParams();
	
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">


<script language="JavaScript" for="EDIT_HTML" event="DisplayChanged">
// EVENT HANDLER: Update the font display on change
return DisplayChanged();
</script>

<script language="JavaScript" for="EDIT_HTML" event="ShowContextMenu">
// EVENT HANDLER: Show a context menu if the right mousebutton is clicked
ShowContextMenu();
</script>

<script language="JavaScript" for="EDIT_HTML" event="ContextMenuAction(itemIndex)">
// EVENT HANDLER: Invoke a context menu action
ContextMenuAction(itemIndex);
</script>


<script type="text/javascript" for="edit1" event="onkeydown">
// EVENT HANDLER: handle tab key in text edit mode
var key = event.keyCode;
switch (key) {
    case 9: 
        // prevent switching focus if tabulator key is pressed
        checkTab();
        break;
}

</script>

<script type="text/javascript">
// Sets some display variables
var USE_LINKSTYLEINPUTS = <%= options.showElement("option.linkstyleinputs", displayOptions) %>;
var USE_FONTFACE = <%= options.showElement("font.face", displayOptions) %>;
var USE_FONTSIZE = <%= options.showElement("font.size", displayOptions) %>; 

// action parameters of the form
var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
var actionSaveExit = "<%= CmsEditor.EDITOR_SAVEEXIT %>";
var actionSave = "<%= CmsEditor.EDITOR_SAVE %>";
var actionSaveAction = "<%= CmsEditor.EDITOR_SAVEACTION %>";

// OpenCms context prefix, required for page editor because no link replacement is done here
var linkEditorPrefix = "<%= wp.getOpenCmsContext() %>";

// Sets the Document Source-Code for later including into the editor
var text="<%= wp.getParamContent() %>";

// URL for pictures
var pfad="<%= wp.getPicsUri() %>";

// Filename 
var filename="<%= wp.getParamResource() %>";

// Workplacepath needed in included javascript files
var workplacePath="<%= cms.link("/system/workplace/") %>";

// Skin URI needed in included javascript files 
var skinUri = "<%= CmsWorkplace.getSkinUri() %>";

// Object for color picker modaldialog
var colorPicker = new Object();
colorPicker.title = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_DIALOG_COLOR_TITLE_0) %>";
colorPicker.color = "000000";

var LANG_CUT = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_CUT_0) %>"; 
var LANG_COPY = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_COPY_0)%>";
var LANG_PASTE = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_PASTE_0)%>";
var LANG_INSERTROW = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_INSERTROW_0)%>";
var LANG_DELETEROW = "<%=  wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_DELETEROW_0)%>";
var LANG_INSERTCOL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_INSERTCOL_0) %>";
var LANG_DELETECOL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_DELETECOL_0)%>";
var LANG_INSERTCELL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_INSERTCELL_0)%>";
var LANG_DELETECELL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_DELETECELL_0)%>";
var LANG_MERGECELL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_MERGECELL_0)%>";
var LANG_SPLITCELL = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_CONTEXT_SPLITCELL_0) %>";

var LANG_NOSELECTION = "<%= wp.key(org.opencms.workplace.editors.Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0)%>";

// popup window holder and counter
var openWindow = null;
var focusCount = 0;

// which button is clicked
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
		openWindow = window.open("about:blank", "PREVIEW", "width=950,height=700,left=10,top=10,resizable=yes,scrollbars=yes,location=yes,menubar=yes,toolbar=yes,dependent=yes");
		focusCount = 1;
		_form.target = "PREVIEW";
		_form.submit();
		break;
	case 3:
		// change element;
		saveContent();
		_form.action.value = "<%= CmsEditor.EDITOR_CHANGE_ELEMENT %>";
		_form.target = "_self";
		_form.submit();
		break;
	case 4:
		// open elements window;
		openWindow = window.open("about:blank", "dialogElementWindow", "width=320,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
		focusCount = 1;
		document.ELEMENTS.submit();
		openWindow.focus();
		break;		
	case 5:
		// open properties window;
		openWindow = window.open("about:blank", "dialogPropertyWindow", "width=600,height=280,left=0,top=0,resizable=yes,scrollbars=yes,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
		focusCount = 1;
		document.PROPERTIES.submit();
		openWindow.focus();
		break;
    case 13:
        // clear document
        saveContent();
		_form.action.value = "<%= CmsEditor.EDITOR_CLEANUP %>";
		_form.target = "_self";
		_form.submit();
        break;		
	}
}

function confirmExitHTML() {
	if (EDITOR.EDIT_HTML.IsDirty) {
		if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0) %>")) {
			doEditHTML(1);
		}
	} else {
		doEditHTML(1);
	}
}

function confirmExit() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0) %>")) {
		doEdit(1);
	}
}

function deleteEditorContent(elementName, language) {
	if (elementName == document.EDITOR.<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>.value && language == document.EDITOR.<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>.value) {
		if (document.EDITOR.EDIT_HTML) {
			document.EDITOR.EDIT_HTML.DocumentHTML = "";
		} else {
			document.getElementById("edit1").innerText = "";	
		}
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

function closeDialog() {
	if (openWindow) {
		window.openWindow.close();
	}
	if (document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value == "null" || document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value == "") {
		top.closeframe.closePage("<%= wp.getParamTempfile() %>", "<%= wp.getParamResource() %>");
	}
}

function opensmallwin(url, name, w, h) {
	encodedurl = encodeURI(url);
	smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(smallwindow != null) {
		if (smallwindow.opener == null) {
			smallwindow.opener = self;
		}
	}
	return smallwindow;
}

function checkPopup() {
	if (openWindow && focusCount > 0) {
		openWindow.focus();
		focusCount = 0;
	}
}

function popupCloseAction(closeObj) {
	if (closeObj.refreshOpener && closeObj.refreshOpener) {
		buttonAction(1);
	} else if (closeObj.elemName) {
		changeElement(closeObj.elemName, closeObj.elemLocale);
	}
}

<%	if (wp.isHelpEnabled()) {
		out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale())); 
	}
%>
</script>

<%

// include JavaScript for WYSIWYG or text mode
if ("edit".equals(wp.getParamEditormode())) {
	%><script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script><%  
} else {
	%><script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edithtml.js"></script>


<script LANGUAGE="vbscript">
' Ugly VB Script that sets the contents of the "Format" selection
' These _must_ be localized or the MS DHTML will not work
' I just don't know how to do this in JavaScript
Sub getStyles()
	' Create the block fmt names holder
	Set fmt = CreateObject("DEGetBlockFmtNamesParam.DEGetBlockFmtNamesParam.1")

	' Get the localized strings for the DECMD_SETBLOCKFMT command
	EDITOR.EDIT_HTML.ExecCommand DECMD_GETBLOCKFMTNAMES, OLECMDEXECOPT_DONTPROMPTUSER, fmt

	' Put the localized strings into a (JavaScript) array
	i = 0
	For Each fmtName In fmt.Names
		setStyles i, fmtName
		i = i + 1
	Next
End Sub
</script>
	<%
}

%>
</head>

<body class="buttons-head" unselectable="on" onload="initContent(); initStyles();" onunload="closeDialog();">

<table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">

<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">

<input type="hidden" name="<%= CmsEditor.PARAM_CONTENT %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_OLDELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
<input type="hidden" name="URL">

<tr><td>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%
boolean showSource = options.showElement("option.sourcecode", displayOptions);
if (showSource) { 
	%><%= wp.buttonBarLabel(org.opencms.workplace.editors.Messages.GUI_INPUT_EDITOR_0) %>
	<td><%= wp.buildSelectViews("class=\"textfeld\" name=\"editormode\" width=\"120\" onchange=\"buttonAction(1);\"") %></td>
	<%= wp.buttonBarSeparator(5, 5) %><%
}
boolean elementSelection = options.showElement("option.element.selection", displayOptions);
boolean elementLanguage = options.showElement("option.element.language", displayOptions);
if (elementSelection || elementLanguage) {
	out.println(wp.buttonBarLabel(org.opencms.workplace.editors.Messages.GUI_INPUT_ELEMENT_0));
	if (elementLanguage) {
		out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + CmsEditor.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
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
	if ((showSource || elementLanguage) && !elementSelection) {
		out.println(wp.buttonBarSeparator(5, 5));
	}
	out.println(wp.button("javascript:buttonAction(5);", null, "properties", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_PROPERTIES_BUTTON_0, buttonStyle));
}
out.println(wp.button("javascript:buttonAction(13);", null, "cleanup", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_CLEANUP_BUTTON_0, buttonStyle));
%>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:buttonAction(2);", null, "preview.png",org.opencms.workplace.editors.Messages.GUI_BUTTON_PREVIEW_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
<%= wp.buttonBarHorizontalLine() %>

<%	
	if (!"edit".equals(wp.getParamEditormode())) {
	    // display the WYSIWYG html editor
	    
%>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%
if (options.showElement("button.customized", displayOptions)) {%>
	<td><%= wp.buttonActionDirectEdit("doEditHTML(55);", buttonStyle) %></td><%
}
%>
<%= wp.button("javascript:doEditHTML(2);", null, "save_exit",org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0, buttonStyle) %>
<%= wp.button("javascript:doEditHTML(3);", null, "save", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:doEditHTML(4);", null, "undo", org.opencms.workplace.editors.Messages.GUI_BUTTON_UNDO_0, buttonStyle) %>
<%= wp.button("javascript:doEditHTML(5);", null, "redo", org.opencms.workplace.editors.Messages.GUI_BUTTON_REDO_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:doEditHTML(6);", null, "editorsearch",org.opencms.workplace.editors.Messages.GUI_BUTTON_SEARCH_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:doEditHTML(9);", null, "cut",org.opencms.workplace.editors.Messages.GUI_BUTTON_CUT_0, buttonStyle) %>
<%= wp.button("javascript:doEditHTML(10);", null, "copy", org.opencms.workplace.editors.Messages.GUI_BUTTON_COPY_0, buttonStyle) %>
<%= wp.button("javascript:doEditHTML(11);", null, "paste", org.opencms.workplace.editors.Messages.GUI_BUTTON_PASTE_0, buttonStyle) %>
<%
if (options.showElement("option.table", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(40);", null, "table", org.opencms.workplace.editors.Messages.GUI_BUTTON_TABLE_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(51);", null, "table_tr",org.opencms.workplace.editors.Messages.GUI_BUTTON_TABLEROW_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(52);", null, "table_td",org.opencms.workplace.editors.Messages.GUI_BUTTON_TABLECELL_0, buttonStyle) %><%
}
if (options.showElement("option.links", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(50);", null, "link", org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(53);", null, "anchor",org.opencms.workplace.editors.Messages.GUI_BUTTON_ANCHOR_0, buttonStyle) %><%
}
if (options.showElement("option.images", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(42);", null, "image", org.opencms.workplace.editors.Messages.GUI_BUTTON_IMAGE_0, buttonStyle) %><%
} %>
<%= wp.buildGalleryButtons(options,buttonStyle,displayOptions) %>
<%
if (options.showElement("option.specialchars", displayOptions)) {
	%><%= wp.button("javascript:doEditHTML(48);", null, "specialchar",org.opencms.workplace.editors.Messages.GUI_BUTTON_SPECIALCHARS_0, buttonStyle) %><%
}
%>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:doEditHTML(47);", null, "details", org.opencms.workplace.editors.Messages.GUI_BUTTON_TOGGLEDETAILS_0, buttonStyle) %>
<%
if (wp.isHelpEnabled()) {%>								
	<%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:openOnlineHelp('/editors/msdhtml');", null, "help.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0, buttonStyle) %><%
} %>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:confirmExitHTML();", null, "exit",org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
<%= wp.buttonBarHorizontalLine() %>
<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<td><select class="textfeld" name="BLOCK" id="BLOCK" width=100 style="width:100px" onchange="doEditHTML(21);" size="1"><option selected value="Normal">Normal</select></td>
<%
		
if (options.showElement("font.face", displayOptions)) {%>
	<%= wp.buttonBarSpacer(5) %>
	<td><%= wp.buildSelectFonts("class=\"textfeld\" name=\"FONTFACE\" width=\"120\" onchange=\"doEditHTML(22);\"") %></td><%
} else {
    // don't display font face selection
%>
	<input type="hidden" name="FONTFACE" style="display: none;"><%
} 
if (options.showElement("font.size", displayOptions)) {
	%><%= wp.buttonBarSpacer(5) %>
	<td><select class="textfeld" name="FONTSIZE" id="FONTSIZE" width="40" style="width:40px" onchange="doEditHTML(23);" size="1"><option selected value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option><option value="6">6</option><option value="7">7</option></select></td><%
} else {
    // don't diaply font size selection
%>
	<input type="hidden" name="FONTSIZE" style="display: none;"><%
}

if (options.showElement("font.decoration", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(24);", null, "bold", org.opencms.workplace.editors.Messages.GUI_BUTTON_BOLD_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(25);", null, "italic",org.opencms.workplace.editors.Messages.GUI_BUTTON_ITALIC_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(26);", null, "underline",org.opencms.workplace.editors.Messages.GUI_BUTTON_UNDERLINE_0, buttonStyle) %><%
} 
if (options.showElement("text.align", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(31);", null, "left",org.opencms.workplace.editors.Messages.GUI_BUTTON_ALIGNLEFT_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(32);", null, "center",org.opencms.workplace.editors.Messages.GUI_BUTTON_ALIGNCENTER_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(33);", null, "right", org.opencms.workplace.editors.Messages.GUI_BUTTON_ALIGNRIGHT_0, buttonStyle) %><%
}
if (options.showElement("text.lists", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(34);", null, "bullist",org.opencms.workplace.editors.Messages.GUI_BUTTON_UL_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(35);", null, "numlist",org.opencms.workplace.editors.Messages.GUI_BUTTON_OL_0, buttonStyle) %><%
}
if (options.showElement("text.indent", displayOptions)) {
	%><%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:doEditHTML(36);", null, "indent", org.opencms.workplace.editors.Messages.GUI_BUTTON_INDENTIN_0, buttonStyle) %>
	<%= wp.button("javascript:doEditHTML(37);", null, "outdent",org.opencms.workplace.editors.Messages.GUI_BUTTON_INDENTOUT_0, buttonStyle) %><%
}

boolean fontColor = options.showElement("font.color", displayOptions);
boolean bgColor = options.showElement("bg.color", displayOptions);			
if (fontColor || bgColor) {%>
	<%= wp.buttonBarSeparator(5, 5) %><%
}
if (fontColor) {%>
	<%= wp.button("javascript:doEditHTML(38);", null, "color_text", org.opencms.workplace.editors.Messages.GUI_BUTTON_FONTCOLOR_0, buttonStyle) %><%
}
if (bgColor) {%>
	<%= wp.button("javascript:doEditHTML(39);", null, "color_fill", org.opencms.workplace.editors.Messages.GUI_BUTTON_COLOR_0, buttonStyle) %><%
}
%>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
</td>
<tr>
<tr>
	<td style="width: 100%; height: 100%;"><OBJECT
		CLASSID="clsid:2D360201-FFF5-11D1-8D03-00A0C959BC0A" 
		ID="EDIT_HTML"
		WIDTH="100%"
		height="100%"
		VIEWASTEXT
		onfocus="checkPopup();">
		<param name="Scrollbars" value="true">
		<param name="ShowBorders" value="true">
	</OBJECT>
	<!-- DEInsertTableParam Object -->
	<object ID="ObjTableInfo" CLASSID="clsid:47B0DFC7-B7A3-11D1-ADC5-006008A5848C" VIEWASTEXT>
	</object>
	<!-- DEGetBlockFmtNamesParam Object -->
	<object ID="ObjBlockFormatInfo" CLASSID="clsid:8D91090E-B955-11D1-ADC5-006008A5848C" VIEWASTEXT>
	</object>
	</td>
</tr>

<%

		// end of WYSIWYG html editor
	} else {
		// display the source code / text editor

%>
<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%
if (options.showElement("button.customized", displayOptions)) {
	%><td><%= wp.buttonActionDirectEdit("doEdit(4);", buttonStyle) %></td><%
}
%>
<%= wp.button("javascript:doEdit(2);", null, "save_exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0, buttonStyle) %>
<%= wp.button("javascript:doEdit(3);", null, "save", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:doEdit(7);", null, "cut", org.opencms.workplace.editors.Messages.GUI_BUTTON_CUT_0, buttonStyle) %>
<%= wp.button("javascript:doEdit(8);", null, "copy", org.opencms.workplace.editors.Messages.GUI_BUTTON_COPY_0, buttonStyle) %>
<%= wp.button("javascript:doEdit(9);", null, "paste", org.opencms.workplace.editors.Messages.GUI_BUTTON_PASTE_0, buttonStyle) %>
<%			
if (wp.isHelpEnabled()) {%>
	<%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:openOnlineHelp('/editors/msdhtmltext');", null, "help.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0, buttonStyle) %><%
} %>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:confirmExit();", null, "exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</td>
<tr>
<tr>
	<td class="texteditor" height="100%" width="100%">
		<textarea id="edit1" name="edit1" style="width:100%; height:100%; background-color: #ffffff; font-family: 'Fixedsys'; margin:0px; padding:0px;"></textarea>
	</td>
</tr>

<%

		// end of source code / text editor
	}

%>	
</form>

<form style="display:none" name="ELEMENTS" action="dialogs/elements.jsp" target="dialogElementWindow" method="post">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ISPOPUP %>" value="true">
</form>

<form style="display:none" name="PROPERTIES" action="../commons/property.jsp" target="dialogPropertyWindow" method="post">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="usetempfileproject" value="true">
<input type="hidden" name="<%= CmsDialog.PARAM_ISPOPUP %>" value="true">
</form>
</table>
</body>
<%= wp.htmlEnd() %>
<%
}
%>