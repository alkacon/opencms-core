<%@page import="org.opencms.ade.publish.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsPublishActionElement jsp = new CmsPublishActionElement(pageContext, request, response);
%><!DOCTYPE HTML>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%= jsp.exportAll()%>
  </head>
  <body>
    <button onclick="cmsShowPublishDialog()">Show publish dialog</button>
  </body>
</html>
