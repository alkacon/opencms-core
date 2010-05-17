<%@page import="org.opencms.ade.galleries.CmsGalleryActionElement"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsGalleryActionElement gallery = new CmsGalleryActionElement(pageContext, request, response);
%>
<!DOCTYPE module PUBLIC "-//Google Inc.//DTD Google Web Toolkit 2.0.3//EN" "http://google-web-toolkit.googlecode.com/svn/tags/2.0.1/distro-source/core/src/gwt-module.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><%= gallery.getTitle() %></title>
    <script type="text/javascript" language="javascript" src="<cms:link>/system/modules/org.opencms.ade.galleries/resources/resources.nocache.js</cms:link>"></script>
    <script><%= gallery.exportAll() %></script>
  </head>
  <body>
  </body>
</html>