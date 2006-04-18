<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
    CmsExplorer wp = new CmsExplorer(cms);
    String paramUri = request.getParameter("uri");
    String body = "explorer_files.jsp?mode=explorerview";
    if (paramUri != null) {
       body = paramUri;
    }
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
</head>

<frameset cols="20%,80%" framespacing="2">
    <frame <%= wp.getFrameSource("explorer_tree", cms.link("tree_fs.jsp")) %>>
    <frame <%= wp.getFrameSource("explorer_files", cms.link(body)) %>>
</frameset>

</html>