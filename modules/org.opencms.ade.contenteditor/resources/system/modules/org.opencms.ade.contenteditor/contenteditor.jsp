<%@page import="org.opencms.ade.contenteditor.CmsContentEditorActionElement"%><%
CmsContentEditorActionElement actionElement=new CmsContentEditorActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
	<head>
	<%= actionElement.exportAll() %>
	</head>
	<body style="width: 90%; margin: 20px auto;">&nbsp;</body>
</html>