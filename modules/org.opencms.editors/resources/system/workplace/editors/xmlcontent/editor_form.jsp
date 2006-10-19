	<%@ page import="
	org.opencms.workplace.editors.*,
	org.opencms.workplace.*,
	org.opencms.jsp.*"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsXmlContentEditor wp = new CmsXmlContentEditor(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsEditor.ACTION_PREVIEW:
//////////////////// ACTION: preview the content

	wp.actionPreview();

break;
case CmsEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_CONFIRMCORRECTION:
//////////////////// ACTION: show confirm dialog to correct the XML structure

	// XML content not valid, create necessary html to show correction confirmation
	%><html>
	<head>
	<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
	<script type="text/javascript">
	<!--
		var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
		var actionCorrectXml = "<%= CmsXmlContentEditor.EDITOR_CORRECTIONCONFIRMED %>";

		function confirmCorrection() {
			var check = confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_XMLCONTENT_CONFIRM_CORRECTION_0) %>");
			if (check) {
				// correct the XML structure
				buttonAction(12);
			} else {
				// exit editor in top frame 
				buttonAction(1);
			}
		}
	//-->
	</script>
	</head>
	<body class="buttons-head" unselectable="on" onload="confirmCorrection();">
	<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp") %>">
	<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="">
	<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
	<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
	<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
	<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
	<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
	</form>
	</body>
	</html>	
	<%
	break;

case CmsEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	if (! wp.hasValidationErrors()) {
		// successfully saved content, close the editor by creating necessary html to submit
		%><html>
		<head>
		<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
		<script type="text/javascript">
		<!--
			var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
		//-->
		</script>
		</head>
		<body class="buttons-head" unselectable="on">
		<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp") %>">
		<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="">
		<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
		<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
		<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
		<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
		<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
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

case CmsEditor.ACTION_DELETELOCALE:
//////////////////// ACTION: delete a localeand show the editor again
	if (wp.getAction() == CmsEditor.ACTION_DELETELOCALE) {
		wp.actionDeleteElementLocale();
        }

case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again
	
	if (wp.getAction() == CmsEditor.ACTION_SAVE) {
		wp.actionSave();
		if (wp.getAction() == CmsDialog.ACTION_CANCEL) {
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

case CmsDialog.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)

wp.setParamAction(null);

 %><html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>Input form</title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>help.js"></script>
<%= wp.getXmlEditorIncludes() %>

<script type="text/javascript">
<!--

// flag indicating if form initialization is finished
var initialized = false;

// the OpenCms context path
var contextPath = "<%= wp.getOpenCmsContext() %>";
// the OpenCms workplace path
var workplacePath="<%= cms.link("/system/workplace/") %>";
// skin URI needed in included javascript files 
var skinUri = "<%= CmsWorkplace.getSkinUri() %>";
// style of the buttons
var buttonStyle = <%= buttonStyle %>;

// needed when strings are filled in delayed
var stringsPresent = false;
var stringsInserted = false;

// action parameters of the form
var actionAddElement = "<%= CmsXmlContentEditor.EDITOR_ACTION_ELEMENT_ADD %>";
var actionChangeElement = "<%= CmsEditor.EDITOR_CHANGE_ELEMENT %>";
var actionCheck = "<%= CmsXmlContentEditor.EDITOR_ACTION_CHECK %>";
var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
var actionPreview = "<%= CmsEditor.EDITOR_PREVIEW %>";
var actionRemoveElement = "<%= CmsXmlContentEditor.EDITOR_ACTION_ELEMENT_REMOVE %>";
var actionSaveAction = "<%= CmsEditor.EDITOR_SAVEACTION %>";
var actionSaveExit = "<%= CmsEditor.EDITOR_SAVEEXIT %>";
var actionSave = "<%= CmsEditor.EDITOR_SAVE %>";
var actionMoveElementDown = "<%= CmsXmlContentEditor.EDITOR_ACTION_ELEMENT_MOVE_DOWN %>";
var actionMoveElementUp = "<%= CmsXmlContentEditor.EDITOR_ACTION_ELEMENT_MOVE_UP %>";
var actionCorrectXml = "<%= CmsXmlContentEditor.EDITOR_CORRECTIONCONFIRMED %>";
var actionDeleteLocale = "<%= CmsEditor.EDITOR_DELETELOCALE %>";

// Localized button labels
var LANG_BT_DELETE = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_DELETE_0) %>";
var LANG_BT_ADD = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_ADDNEW_0) %>";
var LANG_BT_MOVE_UP = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_XMLCONTENT_MOVE_UP_0) %>";
var LANG_BT_MOVE_DOWN = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_XMLCONTENT_MOVE_DOWN_0) %>";

// the currently edited element language
var editedElementLanguage = "<%= wp.getParamElementlanguage() %>";
	
// Ask user whether he really wants to leave the editor without saving
function confirmExit() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0) %>")) {
		buttonAction(1);
	}
}

// Ask user whether he really wants to delete the locale
function confirmDeleteLocale() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_DELETELOCALE_0) %>")) {
		buttonAction(14);
	}
}



function init() {
	checkElementLanguage("<%= wp.getParamElementlanguage() %>");
<%= wp.getXmlEditorInitCalls() %>
	setTimeout("scrollForm();", 200);
	initialized = true;
	//parent.frames[0].location.reload(true);
	parent.frames[0].buttons.submit();
}

function exitEditor() {
	try {
		// close file selector popup if present
		closeTreeWin();
	} catch (e) {}
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
   http_request.open("POST", "<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp") %>?<%= CmsDialog.PARAM_ACTION %>=<%= CmsEditor.EDITOR_CLOSEBROWSER %>&<%= CmsDialog.PARAM_RESOURCE %>=<%= wp.getParamResource() %>&<%= CmsEditor.PARAM_TEMPFILE %>=<%= wp.getParamTempfile() %>", true);
   http_request.send(null);
}

// dummy function for html request
function httpStateDummy() {
	return;
}

<%= wp.getXmlEditorInitMethods() %>

//-->
</script>

</head>
<body class="buttons-head" unselectable="on" onload="init();" onunload="exitEditor();">

<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp") %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
<input type="hidden" name="<%= CmsXmlContentEditor.PARAM_ELEMENTINDEX %>" value="">
<input type="hidden" name="<%= CmsXmlContentEditor.PARAM_ELEMENTNAME %>" value="">
 
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