<%@page import="org.opencms.ade.upload.CmsUploadActionElement"%><%
  CmsUploadActionElement upload = new CmsUploadActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
  <head>
  	<title><%= upload.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= upload.exportAll() %>
  </head>
  <body style="margin: 0px;">&nbsp;</body>
</html>
