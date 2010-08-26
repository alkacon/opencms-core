<%@ page import="org.opencms.ade.sitemap.CmsSitemapActionElement" contentType="text/plain" %><%@
 taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsSitemapActionElement jsp = new CmsSitemapActionElement(pageContext, request, response);
  if (jsp.dumpXml()) {
  	return;
  }
%><!DOCTYPE HTML>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><%= jsp.getTitle() %></title>
    <script type="text/javascript" src="<cms:link>/system/modules/org.opencms.ade.sitemap/resources/resources.nocache.js</cms:link>"></script>
    <%= jsp.exportAll() %>
  </head>
  <body> 
  </body>
</html>
