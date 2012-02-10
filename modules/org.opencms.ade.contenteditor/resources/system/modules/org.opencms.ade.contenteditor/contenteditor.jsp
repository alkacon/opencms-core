<%@page import="org.opencms.ade.contenteditor.CmsContentEditorActionElement"%><%
CmsContentEditorActionElement actionElement=new CmsContentEditorActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
	<head>
	<%= actionElement.exportAll() %>
	</head>
	<body style="margin: 0px;">&nbsp;</body>
</html>