<%@page taglibs="cms" import="org.opencms.ade.contenteditor.CmsContentEditorActionElement"%><%
CmsContentEditorActionElement actionElement=new CmsContentEditorActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
	<head>
	<%= actionElement.exportAll() %>
	<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/opencms_plugin.js</cms:link>"></script>
	</head>
	<body>&nbsp;</body>
</html>

