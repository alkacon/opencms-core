<%@page import="org.opencms.ade.postupload.*" %><%
CmsPostUploadDialogActionElement dialog = new CmsPostUploadDialogActionElement(pageContext, request, response);
dialog.setUsePropertyConfiguration(true);
%><!DOCTYPE html> 
<html>
  <head>
  	<title>Properties</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= dialog.exportAll() %>
  </head>
  <body style="margin: 0px;">&nbsp;</body>
</html> 
