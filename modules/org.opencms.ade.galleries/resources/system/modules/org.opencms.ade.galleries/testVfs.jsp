<%@page import="org.opencms.ade.galleries.CmsGalleryActionElement"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsGalleryActionElement jsp = new CmsGalleryActionElement(pageContext, request, response);
%>
<!DOCTYPE module PUBLIC "-//Google Inc.//DTD Google Web Toolkit 2.0.3//EN" "http://google-web-toolkit.googlecode.com/svn/tags/2.0.1/distro-source/core/src/gwt-module.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><%= jsp.getTitle() %></title>
    <script type="text/javascript" language="javascript" src="<cms:link>/system/modules/org.opencms.ade.galleries/resources/resources.nocache.js</cms:link>"></script>
    <script><%= jsp.getData() %></script>
  </head>

  <body>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

  </body>
</html>