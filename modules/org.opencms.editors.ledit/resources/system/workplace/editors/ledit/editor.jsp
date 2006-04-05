<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*
"%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsLEditEditor wp = new CmsLEditEditor(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
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
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsDialog.ACTION_CANCEL) {
		// an error occured during save
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
		if (EDITOR.edit1.IsModified(2)) {
			if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0) %>")) {
				buttonAction(1);
			}
		} else {
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
<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogUri() %>">
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
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:buttonAction(4);", null, "undo", org.opencms.workplace.editors.Messages.GUI_BUTTON_UNDO_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(5);", null, "redo", org.opencms.workplace.editors.Messages.GUI_BUTTON_REDO_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:buttonAction(6);", null, "editorsearch",org.opencms.workplace.editors.Messages.GUI_BUTTON_SEARCH_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(7);", null, "replace",org.opencms.workplace.editors.Messages.GUI_BUTTON_REPLACE_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(8);", null, "goto",org.opencms.workplace.editors.Messages.GUI_BUTTON_GOTO_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:buttonAction(9);", null, "cut", org.opencms.workplace.editors.Messages.GUI_BUTTON_CUT_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(10);", null, "copy",org.opencms.workplace.editors.Messages.GUI_BUTTON_COPY_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(11);", null, "paste", org.opencms.workplace.editors.Messages.GUI_BUTTON_PASTE_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:buttonAction(12);", null, "print", org.opencms.workplace.editors.Messages.GUI_BUTTON_PRINT_0, buttonStyle) %>
<%

if (wp.isHelpEnabled()) {%>
	<%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:openOnlineHelp('/editors/ledit');", null, "help.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0, buttonStyle) %><%
} %>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:confirmExit();", null, "exit",  org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
</td>
<tr>
<tr>
	<td class="texteditor" height="100%" width="100%">
		<OBJECT classid="clsid:EB3A74C0-5343-101D-BB4D-040224009C02" width="100%" height="100%" id="edit1" name="edit1" onFocus="setContentDelayed();">
			<PARAM NAME="_StockProps" VALUE="125">
			<PARAM NAME="Text" VALUE="Loading .....">
			<PARAM NAME="ForeColor" VALUE="0">
			<PARAM NAME="BackColor" VALUE="16777215">
			<PARAM NAME="BorderStyle" VALUE="1">
			<PARAM NAME="Enabled" VALUE="-1">
			<PARAM NAME="AutoIndent" VALUE="-1">
			<PARAM NAME="BackColorSelected" VALUE="8388608">
			<PARAM NAME="BookmarksMax" VALUE="16">
			<PARAM NAME="CanChangeFile" VALUE="0">
			<PARAM NAME="CanChangeFont" VALUE="0">
			<PARAM NAME="CaretWidth" VALUE="0">
			<PARAM NAME="DefaultSelection" VALUE="-1">
			<PARAM NAME="ExtraComments" VALUE="0">
			<PARAM NAME="Item2AsComment" VALUE="0">
			<PARAM NAME="UnixStyleSave" VALUE="0">
			<PARAM NAME="CurrentWordAsText" VALUE="0">
			<PARAM NAME="MultilineItems" VALUE="0">
			<PARAM NAME="MultilineStrings" VALUE="0">
			<PARAM NAME="SinglelineStrings" VALUE="0">
			<PARAM NAME="ExtraHorzSpacing" VALUE="3">
			<PARAM NAME="ExtraVertSpacing" VALUE="0">
			<PARAM NAME="FileMask" VALUE="/All Files/*.*/">
			<PARAM NAME="FileName" VALUE="+">
			<PARAM NAME="ForeColorSelected" VALUE="16777215">
			<PARAM NAME="HasFile" VALUE="-1">
			<PARAM NAME="HasMenu" VALUE="0">
			<PARAM NAME="Highlight" VALUE="0">
			<PARAM NAME="InsertMode" VALUE="1">
			<PARAM NAME="ScrollBars" VALUE="3">
			<PARAM NAME="Syntax" VALUE="">
			<PARAM NAME="PaintMode" VALUE="0">
			<PARAM NAME="TabStopSize" VALUE="4">
			<PARAM NAME="ReadOnly" VALUE="0">
			<PARAM NAME="UndoDepth" VALUE="-1">
			<PARAM NAME="InitializeType" VALUE="0">
			<PARAM NAME="GroupNumber" VALUE="0">
			<PARAM NAME="StartInComments" VALUE="0">
			<PARAM NAME="MacStyleSave" VALUE="0">
			<PARAM NAME="WantTab" VALUE="-1">
			<PARAM NAME="WordWrap" VALUE="0">
			<PARAM NAME="WordWrapAuto" VALUE="-1">
			<PARAM NAME="WordWrapRMargin" VALUE="0">
			<PARAM NAME="WordWrapWidth" VALUE="0">
			<PARAM NAME="NoPrintDialog" VALUE="0">
			<PARAM NAME="NoPrintProgress" VALUE="0">
			<PARAM NAME="WrapKeys" VALUE="0">
			<PARAM NAME="BackColorPrint" VALUE="16777215">
			<PARAM NAME="PrintJobName" VALUE="Document">
			<PARAM NAME="ZeroSubstitute" VALUE="0">
		</OBJECT>
		<script type="text/javascript">
		<!--
			if (document.edit1 != true) {
				document.location=document.location+"&<%= CmsEditor.PARAM_LOADDEFAULT %>=true";
			}
		// -->
		</script>
	</td>
</tr>
</form>
</table>
</body>
</html>
<%
}
%>