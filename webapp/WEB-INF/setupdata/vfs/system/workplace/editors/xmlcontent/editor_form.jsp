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

case CmsXmlContentEditor.ACTION_CANCEL:
case CmsXmlContentEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsXmlContentEditor.ACTION_PREVIEW:
//////////////////// ACTION: preview the content

	wp.actionPreview();

break;
case CmsXmlContentEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	if (! wp.hasValidationErrors()) {
		// successfully saved content, close the editor by creating necessary html to submit
		%><html>
		<head>
		<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
		<script type="text/javascript">
		<!--
			var actionExit = "<%= wp.EDITOR_EXIT %>";
		//-->
		</script>
		</head>
		<body class="buttons-head" unselectable="on">
		<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getJsp().link(CmsEditor.C_PATH_EDITORS + "xmlcontent/editor_form.jsp") %>">
		<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="">
		<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
		<input type="hidden" name="<%= wp.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
		<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
		<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
		</form>
		<script type="text/javascript">
		<!--
			// exit editor in top frame 
			buttonAction(1);
		//-->
		</script>
		</body>
		</html>	
		<%
		break;
	}


case CmsXmlContentEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again
	
	if (wp.getAction() == CmsXmlContentEditor.ACTION_SAVE) {
		wp.actionSave();
		if (wp.getAction() == CmsXmlContentEditor.ACTION_CANCEL) {
			// an error occured during save, do not show editor form
			break;
		}
	}

case CmsXmlContentEditor.ACTION_NEW:
//////////////////// ACTION: create a new content item

	if (wp.getAction() == CmsXmlContentEditor.ACTION_NEW) {
		// required since a save action continues with this code
		wp.actionNew();
		// new continues with editing the new resource
	}
	
case CmsXmlContentEditor.ACTION_CHECK:
//////////////////// ACTION: check the content before the directedit action is executed and display form if there are errors

	if (wp.getAction() == CmsXmlContentEditor.ACTION_CHECK) {
		wp.setEditorValues(wp.getElementLocale());
	}

case CmsXmlContentEditor.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)
%><html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>Input form</title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri(wp.getJsp(),"workplace.css") %>">

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>help.js"></script>
<%= wp.getXmlEditorIncludes() %>

<script type="text/javascript">
<!--

// flag indicating if form initialization is finished
var initialized = false;

// the OpenCms context path
var contextPath = "<%= wp.getOpenCmsContext() %>";

// needed when strings are filled in delayed
var stringsPresent = false;
var stringsInserted = false;

// action parameters of the form
var actionAddElement = "<%= wp.EDITOR_ACTION_ELEMENT_ADD %>";
var actionChangeElement = "<%= wp.EDITOR_CHANGE_ELEMENT %>";
var actionCheck = "<%= wp.EDITOR_ACTION_CHECK %>";
var actionExit = "<%= wp.EDITOR_EXIT %>";
var actionPreview = "<%= wp.EDITOR_PREVIEW %>";
var actionRemoveElement = "<%= wp.EDITOR_ACTION_ELEMENT_REMOVE %>";
var actionSaveAction = "<%= wp.EDITOR_SAVEACTION %>";
var actionSaveExit = "<%= wp.EDITOR_SAVEEXIT %>";
var actionSave = "<%= wp.EDITOR_SAVE %>";
	
// Ask user whether he really wants to leave the editor without saving
function confirmExit() {
	if (confirm("<%= wp.key("editor.message.exit") %>")) {
		buttonAction(1);
	}
}

function init() {
	checkElementLanguage("<%= wp.getParamElementlanguage() %>");
<%= wp.getXmlEditorInitCalls() %>
	setTimeout("scrollForm();", 100);
	initialized = true;
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
<input type="hidden" name="<%= wp.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= wp.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= wp.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= wp.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= wp.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= wp.PARAM_ELEMENTINDEX %>" value="">
<input type="hidden" name="<%= wp.PARAM_ELEMENTNAME %>" value="">
 
<%= wp.getXmlEditorForm() %>

</form>

<%= wp.getXmlEditorHtmlEnd() %><%

if ((wp.getAction() == CmsXmlContentEditor.ACTION_CHECK) && (! wp.hasValidationErrors())) {
	// automatically submit form after loading it for validation when button "customized action" was pressed
	%>
	<script type="text/javascript">
	<!--
		setTimeout('submitSaveAction();', 20);
	//-->
	</script>
	<%
}

%>
</body>
</html><%
}
%>