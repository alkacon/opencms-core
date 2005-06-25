<%@ page import="
	org.opencms.jsp.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*,
	java.util.*"
	session="false"
%><%
	
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

// stores the scroll target y coordinate when adding/removing an element in the input form
var lastPosY = 0;
// the frame of the input form
var formFrame = top.edit.editform;

function buttonAction(actionValue) {
	lastPosY = 0;
	formFrame.buttonAction(actionValue);
}

function changeElementLanguage() {
	formFrame.document.forms["EDITOR"].elements["<%= wp.PARAM_ELEMENTLANGUAGE %>"].value = document.forms["buttons"].elements["<%= wp.PARAM_ELEMENTLANGUAGE %>"].value;
	buttonAction(4);
	formFrame.focus();
}

function confirmExit() {
	formFrame.confirmExit();
}
//-->
</script>

</head>
<body class="buttons-head" unselectable="on">

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>

<form name="buttons" action="" method="post">

<%
if (options.showElement("button.customized", displayOptions)) {%>
	<td><%= wp.buttonActionDirectEdit("buttonAction(8);", buttonStyle) %></td><%
}
%>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit", "button.saveclose", buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", "button.save", buttonStyle) %>


<%

if (options.showElement("option.element.language", displayOptions) && wp.showElementLanguageSelector()) {
	out.println(wp.buttonBarSeparator(5, 5));
	out.println(wp.buttonBarLabel("input.lang"));
	out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + wp.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"changeElementLanguage();\"") + "</td>");
}

%>
	<td class="maxwidth">&nbsp;</td>
<%

if (wp.isPreviewEnabled()) {
	// show preview button if enabled
	out.println(wp.button("javascript:buttonAction(7);", null, "preview.png", "button.preview", buttonStyle));
	out.println(wp.buttonBarSeparator(5, 5));
}

%>

<td width="100%">&nbsp;</td>
		
<%= wp.button("javascript:confirmExit();", null, "exit", "button.close", buttonStyle) %>

</form>

<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(wp.HTML_END) %>

</body>
</html>