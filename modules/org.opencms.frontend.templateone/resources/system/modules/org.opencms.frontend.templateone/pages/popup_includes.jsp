<%@ page session="false" buffer="none" import="org.opencms.i18n.*,org.opencms.jsp.*" %><%

// initialise Cms Action Element
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

String locale = cms.getRequestContext().getLocale().getLanguage();

if (cms.template("popuphead")) {

	CmsMessages messages = cms.getMessages("templateone", locale);
	
	String title = request.getParameter("title");
	String styleSheetUri = request.getParameter("stylesheeturi");
	String resourcePath = request.getParameter("resourcepath");
	String conf = request.getParameter("config");

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="<%= locale %>">
<head>
	<title><%= title %></title>
	<link href="<%= styleSheetUri %>?respath=<%=resourcePath %>&config=<%= conf %>&site=<%= cms.getRequestContext().getSiteRoot() %>&__locale=<%= locale %>" rel="stylesheet" type="text/css">
	<script type="text/javascript">
	<!--
    function showFile(filename) {
        window.opener.location.href = filename;
        window.opener.focus();
    }
	//-->
	</script>
	<% cms.editable(true); %>
</head>
<body style="font-size: 80%;">
<p style="font-size: 80%; text-align: right; margin-top: 5px; line-height: 1px;">
	<a href="javascript:window.close();"><%= messages.key("link.close") %></a>
</p>
<% 
}


if (cms.template("popupfoot")) {

%></body>
</html><%
}
%>