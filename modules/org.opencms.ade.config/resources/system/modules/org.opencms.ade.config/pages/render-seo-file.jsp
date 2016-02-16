<%@page import="org.opencms.site.xmlsitemap.CmsXmlSitemapActionElement" %><%
CmsXmlSitemapActionElement actionElement = new CmsXmlSitemapActionElement(pageContext, request, response);
actionElement.run();
%>