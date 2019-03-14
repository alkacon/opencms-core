<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

    if (Bean.isInitialized()) {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>Alkacon OpenCms Setup Wizard</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="refresh" content="0;url=<%=Bean.getDatabaseConfigPage(Bean.getDatabase()) %>">
</head>
<body>
</body>
</html>
<%	} else { %>
<%= Bean.getHtmlPart("C_HTML_START") %>
Alkacon OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
Alkacon OpenCms Setup Wizard - Database selection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<%= Bean.getHtmlPart("C_HTML_END") %>
<% } %>
