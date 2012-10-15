<%@page taglibs="cms" import="org.opencms.ade.contenteditor.CmsContentEditorActionElement"%><%
CmsContentEditorActionElement actionElement=new CmsContentEditorActionElement(pageContext, request, response);
pageContext.setAttribute("actionElement", actionElement);
%><c:choose><c:when test="${actionElement.newEditorSupported}"><!DOCTYPE html>
<html>
	<head>
	<%= actionElement.exportAll() %>
	<script type="text/javascript" src="<cms:link>/system/workplace/editors/tinymce/opencms_plugin.js</cms:link>"></script>
	</head>
	<body>&nbsp;</body>
</html></c:when>
<c:otherwise><cms:include page="/system/workplace/editors/xmlcontent/editor.jsp" /></c:otherwise></c:choose>
