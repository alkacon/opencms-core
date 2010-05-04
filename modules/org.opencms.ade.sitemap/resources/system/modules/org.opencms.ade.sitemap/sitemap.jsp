<%@page import="org.opencms.ade.sitemap.CmsSitemapActionElement"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsSitemapActionElement jsp = new CmsSitemapActionElement(pageContext, request, response);
%><!DOCTYPE HTML>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><%= jsp.getTitle() %></title>
    <script src="<cms:link>/system/modules/org.opencms.ade.sitemap/resources/resources.nocache.js</cms:link>"></script>
    <script><%= jsp.exportAll() %></script>
  </head>

  <body>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
  </body>
</html>