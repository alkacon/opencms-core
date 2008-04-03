<%@ page import="
	org.opencms.editors.editarea.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsEditArea wp = new CmsEditArea(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;
case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsDialog.ACTION_CANCEL) {
		// an error occurred during save
		break;
	}

case CmsDialog.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">

<script type="text/javascript">
	// Sets the Document Source-Code for later including into the editor
	var content="<%= wp.getParamContent() %>";

	// Workplacepath needed in included javascript files
	var workplacePath="<%= cms.link("/system/workplace/") %>";
	
	// action parameters of the form
	var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
	var actionSaveExit = "<%= CmsEditor.EDITOR_SAVEEXIT %>";
	var actionSave = "<%= CmsEditor.EDITOR_SAVE %>";

	// Ask user whether he really wants to leave Texteditor without saving
	function confirmExit()
	{
		if (confirm ("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
			buttonAction(1);
		}
	}
	
<%	if (wp.isHelpEnabled()) {
		out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale())); 
	}
%>
</script>

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
<%-- cannot use edit_area_loader.js here because of IE 7 issues --%>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>dist/edit_area_full.js"></script>
<script language="Javascript" type="text/javascript">
	
	// initialisation
	editAreaLoader.init({
		id: "editarea"	// id of the textarea to transform
		,start_highlight: true
		,font_size: "10"
		,font_family: "courier new, monospace, Fixedsys"
		,allow_resize: "no"
		,allow_toggle: false
		,language: "<%= wp.getEditorLanguage() %>"
		,syntax: "<%= wp.getStartHighlight() %>"
		,syntax_selection_allow: "css,html,js,jsp,xml"
		,toolbar: "|, charmap, |, search, go_to_line, |, undo, redo, |, select_font, |, syntax_selection, |, change_smooth_selection, highlight, reset_highlight, |, help"
		,begin_toolbar: "|, ocms_save_exit, ocms_save"
		,end_toolbar: "|, ocms_exit"
		,save_callback: "my_save"
		,plugins: "charmap,opencms"
		,charmap_default: "arrows"

	});

	// callback functions
	function my_save(id, content){
		buttonAction(3);
	}	

</script>

</head>
<body class="buttons-head" unselectable="on" onload="setContent();">
<div>
<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_CONTENT %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">

<textarea wrap="virtual" id="editarea" name="edit1" style="width: 100%; height: 100%; color: Window; background-color: Window; visibility: hidden;"></textarea>
</form>
</div>
</body>
</html>	
	
<%
}
%>