<%@ page import="org.opencms.ade.sitemap.CmsSitemapActionElement,org.opencms.ui.shared.CmsVaadinConstants" contentType="text/html" %><%
  CmsSitemapActionElement jsp = new CmsSitemapActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html style="height:100%;">
  <head>
    <title><%= jsp.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= jsp.exportAll() %>
  </head>
  <body class="opencms sitemap">
  	<div id="sitemap-ui" class="v-app" style="position: absolute; left: -5000px; top: -5000px; width: 50px; height: 50px; ">
  	</div> 
  </body>
</html>