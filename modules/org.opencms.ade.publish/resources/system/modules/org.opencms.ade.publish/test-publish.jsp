<%@page import="org.opencms.ade.publish.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsPublishActionElement jsp = new CmsPublishActionElement(pageContext, request, response);
%>
<!DOCTYPE module PUBLIC "-//Google Inc.//DTD Google Web Toolkit 2.0.3//EN" "http://google-web-toolkit.googlecode.com/svn/tags/2.0.1/distro-source/core/src/gwt-module.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <script type="text/javascript" language="javascript" src="<cms:link>/system/modules/org.opencms.ade.publish/resources/resources.nocache.js</cms:link>"></script>
    <script><%= jsp.getData() %></script>
  </head>

  <body>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    <button onclick="cmsShowPublishDialog()">Show publish dialog</button>
  </body>
</html>