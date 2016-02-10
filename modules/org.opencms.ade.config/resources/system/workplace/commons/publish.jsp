<%@page import="org.opencms.ade.publish.CmsPublishActionElement"%><%
CmsPublishActionElement publish = new CmsPublishActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
  <head>
  	<title><%= publish.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= publish.exportAll() %>
  </head>
  <body style="margin: 0px;">&nbsp;</body>
</html>