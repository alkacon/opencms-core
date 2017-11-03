<%@page import="org.opencms.site.xmlsitemap.CmsXmlSitemapActionElement" session="false" %><%
CmsXmlSitemapActionElement actionElement = new CmsXmlSitemapActionElement(pageContext, request, response);
actionElement.run();
%>