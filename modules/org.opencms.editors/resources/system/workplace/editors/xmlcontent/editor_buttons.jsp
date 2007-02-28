<%@ page session="false" import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	java.util.*
"%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsXmlContentEditor wp = new CmsXmlContentEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

 %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">

<script type="text/javascript">
<!--

// the frame of the input form
var formFrame = top.edit.editform;

function buttonAction(actionValue) {
	lastPosY = 0;
	formFrame.buttonAction(actionValue);
}

function changeElementLanguage() {
	formFrame.document.forms["EDITOR"].elements["<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>"].value = document.forms["buttons"].elements["<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>"].value;
	buttonAction(4);
	formFrame.focus();
}

function confirmExit() {
	formFrame.confirmExit();
}


function confirmDeleteLocale() {
	formFrame.confirmDeleteLocale();
}

//-->
</script>

</head>
<body class="buttons-head" unselectable="on">

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>

<form name="buttons" action="" method="post">

<%
if (options.showElement("button.customized", displayOptions)) {%>
	<td><%= wp.buttonActionDirectEdit("buttonAction(8);", buttonStyle) %></td><%
}
%>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit",org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0, buttonStyle) %>


<%

if (options.showElement("option.element.language", displayOptions) && wp.showElementLanguageSelector()) {
	out.println(wp.buttonBarSeparator(5, 5));
	out.println(wp.buttonBarLabel(org.opencms.workplace.editors.Messages.GUI_INPUT_LANG_0));
	out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + CmsEditor.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"changeElementLanguage();\"") + "</td>");
	out.println(wp.deleteLocaleButton("javascript:confirmDeleteLocale();", null, "deletecontent", org.opencms.workplace.editors.Messages.GUI_BUTTON_DELETE_0, buttonStyle));
}

%>
	<td class="maxwidth">&nbsp;</td>
<%

if (wp.isPreviewEnabled()) {
	// show preview button if enabled
	out.println(wp.button("javascript:buttonAction(7);", null, "preview.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_PREVIEW_0, buttonStyle));
	out.println(wp.buttonBarSeparator(5, 5));
}

%>

<td width="100%">&nbsp;</td>
		
<%= wp.button("javascript:confirmExit();", null, "exit",org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>

</form>

<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</body>
</html>