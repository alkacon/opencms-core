<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*"
	buffer="none"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsEditorDisplayOptions options = new CmsEditorDisplayOptions(cms.getCmsObject());
CmsXmlContentEditor wp = new CmsXmlContentEditor(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsXmlContentEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsXmlContentEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsXmlContentEditor.ACTION_CANCEL) {
		// an error occured during save
		break;
	}

case CmsXmlContentEditor.ACTION_NEW:
//////////////////// ACTION: create a new content item

	if (wp.getAction() == CmsXmlContentEditor.ACTION_NEW) {
		// required since a save action continues with this code
		wp.actionNew();
		// new continues with editing the new resource
	}

case CmsXmlContentEditor.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getSkinUri() %>commons/css_workplace.css">

<style type="text/css">
<!--
.xmlTable { width:100%; }
.xmlTd    { width:100%; }
.xmlLabel { font-family:verdana, sans-serif; font-size:11px; font-weight:bold;  }
.xmlInput { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; width:100%; }
-->
</style>

<script type="text/javascript">
<!--

// Workplacepath needed in included javascript files
var workplacePath="<%= cms.link("/system/workplace/") %>";
	
// action parameters of the form
var actionExit = "<%= wp.EDITOR_EXIT %>";
var actionSaveExit = "<%= wp.EDITOR_SAVEEXIT %>";
var actionSave = "<%= wp.EDITOR_SAVE %>";
	
// Ask user whether he really wants to leave Texteditor without saving
function confirmExit()
{
	if (confirm("<%= wp.key("editor.message.exit") %>")) {
		buttonAction(1);
	}
}

// function action on button click
function buttonAction(para) {
	var _form = document.EDITOR;

	switch (para) {
	case 1: 
		_form.action.value = actionExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 2:
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 3:
		_form.action.value = actionSave;
		submit(_form);
		_form.submit();
		break;
	default:
		alert("No action defined for this button!");
		break;
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

function init() {
	initHtmlArea();
}

function submit(form) {	
	submitHtmlArea(form);
}

//-->
</script>

<%= wp.calendarIncludes() %>

<script type="text/javascript">
   _editor_url = "<%= wp.getSkinUri() + "editors/htmlarea/" %>";
   _editor_lang = "en";
</script>

<script type="text/javascript" src="<%= wp.getSkinUri() + "editors/htmlarea/" %>htmlarea.js"></script>

<script type="text/javascript">
<!--

// HTMLArea configuration
var config;
var htmlAreas = new Array();
var textAreas = new Array();

function initHtmlArea() {

config = new HTMLArea.Config();
config.toolbar = [
[
"copy", "cut", "paste", "separator",
"bold", "italic", "underline", "separator",
"strikethrough", "subscript", "superscript", "separator",
"insertorderedlist", "insertunorderedlist", "outdent", "indent", "separator",
"htmlmode"
]
];

config.pageStyle = 
	'body { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; margin: 0 0 0 0; padding: 1 1 1 1; border-width:2px; border-color:#FFF; border-style:inset; } ';

// disable the status bar
config.statusBar = false;

// kill MS Word formatting on paste
config.killWordOnPaste = true;

var tas = document.getElementsByTagName("textarea");
for (var i=0; i<tas.length; i++) {
	textAreas[textAreas.length] = tas[i];
	var ha = new HTMLArea(tas[i], config);
	htmlAreas[htmlAreas.length] = ha;
	ha.generate();
}
}

function registerHtmlArea(id) {
}

function submitHtmlArea(form) {
for (var i=0; i<textAreas.length; i++) {
	ta = textAreas[i];
	ha = htmlAreas[i];
	ta.value = encodeURIComponent(ha.getHTML());
}
}


//-->
</script>

</head>
<body class="buttons-head" unselectable="on" onload="init();">
 
<table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogUri() %>">
<input type="hidden" name="<%= wp.PARAM_CONTENT %>" value="<%= wp.getParamContent() %>">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= wp.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= wp.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
	
<tr>
	<td>

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit", "button.saveclose", buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", "button.save", buttonStyle) %>

	</td>
	<td class="maxwidth">&nbsp;</td>
		
<%= wp.button("javascript:confirmExit();", null, "exit", "button.close", buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(wp.HTML_END) %>

	</td>
</tr>

<tr><td width="100%">
<%= wp.getXmlEditor() %>
</td></tr>

<tr><td height="100%" width="100%">&nbsp;</td></tr>

</form>
</table>
</body>
</html>
	
<%
}
%>