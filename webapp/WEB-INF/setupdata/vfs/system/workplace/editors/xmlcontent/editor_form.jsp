<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*,
	java.util.*"
	buffer="none"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsXmlContentEditor wp = new CmsXmlContentEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

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
%><html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>Input form</title>

<link rel=stylesheet type="text/css" href="<%= wp.getSkinUri() %>commons/css_workplace.css">

<style type="text/css">
<!--
.xmlTable { width:100%; }
.xmlTd    { width: 100%; height: 22px; }
.xmlLabel { font-family:verdana, sans-serif; font-size:11px; font-weight:bold; height: 22px; white-space: nowrap; }
.xmlInput { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; }
.xmlInputSmall { width: 200px; }
.xmlInputMedium { width: 400px; }
-->
</style>

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>help.js"></script>
<%= wp.getXmlEditorIncludes() %>

<script type="text/javascript">
<!--

// action parameters of the form
var actionExit = "<%= wp.EDITOR_EXIT %>";
var actionSaveExit = "<%= wp.EDITOR_SAVEEXIT %>";
var actionSave = "<%= wp.EDITOR_SAVE %>";
	
// Ask user whether he really wants to leave the editor without saving
function confirmExit() {
	if (confirm("<%= wp.key("editor.message.exit") %>")) {
		buttonAction(1);
	}
}

function init() {
<%= wp.getXmlEditorInitCalls() %>
}

function exitEditor() {
	try {
		// close file selector popup if present
		closeTreeWin();
	} catch (e) {}
}

<%= wp.getXmlEditorInitMethods() %>

//-->
</script>

</head>
<body class="buttons-head" unselectable="on" onload="init();" onunload="exitEditor();">

<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getJsp().link(CmsEditor.C_PATH_EDITORS + "xmlcontent/editor_form.jsp") %>">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= wp.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= wp.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
 
<%= wp.getXmlEditorForm() %>

</form>

<%= wp.getXmlEditorHtmlEnd() %>

</body>
</html><%
}
%>