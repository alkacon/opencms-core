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
<script language="JavaScript">
<%
	String files = wp.getFileList();
	// System.err.println(files);
	out.println(files);
%>
</script>
</head>

<body onload="initialize();">
</body>

</html>