<%@ page import="org.opencms.ade.sitemap.CmsSitemapActionElement" contentType="text/plain" %><%@
 taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsSitemapActionElement jsp = new CmsSitemapActionElement(pageContext, request, response);
%><!DOCTYPE HTML>
<html>
  <head>
    <title><%= jsp.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= jsp.exportAll() %>
    <script type="text/javascript" src="<cms:link>/system/modules/org.opencms.ade.sitemap/resources/resources.nocache.js</cms:link>"></script>
  </head>
  <body> 
  </body>
</html>
