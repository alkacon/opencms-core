<%@page import="org.opencms.ade.contenteditor.CmsContentEditorActionElement"%><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%
CmsContentEditorActionElement actionElement=new CmsContentEditorActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
	<head>
	<%= actionElement.exportAll() %>
	<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/opencms_plugin.js</cms:link>"></script>
	</head>
	<body>&nbsp;</body>
</html>

