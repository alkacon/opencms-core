<%@page import="org.opencms.ade.postupload.*" %><%
CmsPostUploadDialogActionElement dialog = new CmsPostUploadDialogActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
  <head>
  	<%= dialog.exportAll() %>
  	<title>Properties</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  </head>
  <body style="margin: 0px;">&nbsp;</body>
</html>
