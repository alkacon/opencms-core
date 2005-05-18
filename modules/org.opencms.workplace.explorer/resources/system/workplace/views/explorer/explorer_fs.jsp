<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
	buffer="none"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsExplorer wp = new CmsExplorer(cms);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
</head>

<frameset rows="24,*" border="0" frameborder="0" framespacing="0">
    <frame name="explorer_head" src="<%= wp.getSkinUri() %>commons/empty.html" noresize scrolling="no">
    <frame <%= wp.getFrameSource("explorer_body", cms.link("explorer_body_fs.jsp")) %> noresize scrolling="no">
</frameset>

</html>