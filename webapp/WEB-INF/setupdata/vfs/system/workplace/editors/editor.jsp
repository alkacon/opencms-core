<%@ page import="
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*"
	buffer="none"
	session="false"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsEditorFrameset wp = new CmsEditorFrameset(cms);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamEditorTitle() %></title>
<script type="text/javascript">
<!--
	// change window name when opening editor in direct edit mode 
	// to avoid loss of content when previewing another resource in Explorer view
	if (window.name == "preview") {
		window.name = "editor_directedit";
	}
//-->
</script>
</head>

<frameset rows="*,24,0" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("edit", cms.link("editor_main.jsp?" + wp.getParamsAsRequest())) %> noresize scrolling="no">
    <frame <%= wp.getFrameSource("foot", cms.link("/system/workplace/views/top_foot.jsp")) %> noresize scrolling="no">
    <frame <%= wp.getFrameSource("closeframe", cms.link("editor_close.jsp")) %> noresize scrolling="no">
</frameset>
</html>