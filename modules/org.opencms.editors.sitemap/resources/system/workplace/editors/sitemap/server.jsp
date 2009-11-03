<%@page buffer="none" session="false" import="org.opencms.workplace.editors.sitemap.CmsSitemapServer" %><%

  CmsSitemapServer sitemap = new CmsSitemapServer(pageContext, request, response);
  sitemap.serve();
%>