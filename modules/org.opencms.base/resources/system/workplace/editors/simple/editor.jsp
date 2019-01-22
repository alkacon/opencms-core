<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsSimpleEditor wp = new CmsSimpleEditor(cms);

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
<!--
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
//-->
</script>

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>

</head>
<body class="buttons-head" unselectable="on" onload="setContent();">

<table cellspacing="0" cellpadding="0" border="0" width="100%" height="100%">
<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_CONTENT %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">
	<tr>
		<td>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0, buttonStyle) %>
<%
if (wp.isHelpEnabled()) {%>
	<%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:openOnlineHelp('/editors/simple');", null, "help.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0, buttonStyle) %><%
} %>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:confirmExit();", null, "exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %> 

	</td>
</tr>
<tr>
<td class="texteditor" height="100%" width="100%">
	<textarea wrap="off" name="edit1" id="edit1" onfocus="setContentDelayed();" style="width:100%; height:100%; font-family:Fixedsys, monospace"></textarea>
</td>
</tr>
</form>
</table>
</body>
</html>	
	
<%
}
%>