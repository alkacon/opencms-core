<%@ page import="
	org.opencms.jsp.*,
	org.opencms.workplace.editors.*"
	buffer="none"
	session="false"
%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsEditorFrameset wp = new CmsEditorFrameset(cms);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= cms.getRequestContext().currentUser().getName() %>) - <%= wp.getParamResource() %></title>
</head>

<frameset rows="24,*" border="0" frameborder="0" framespacing="0">
    <frame src="<%= wp.getJsp().link(CmsEditor.C_PATH_EDITORS + "xmlcontent/editor_buttons.jsp") %>" name="buttonbar" noresize scrolling="no">
    <frame src="<%= wp.getJsp().link(CmsEditor.C_PATH_EDITORS + "xmlcontent/editor_form.jsp?" + wp.getParamsAsRequest()) %>" name="editform" noresize scrolling="auto">
</frameset>

</html>