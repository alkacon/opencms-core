<%@page import="org.opencms.file.*" %><%@page import="org.opencms.jsp.*" %><%@page import="org.opencms.ade.sitemap.*" %><%
CmsJspActionElement jsae = new CmsJspActionElement(pageContext, request, response);
CmsObject cms = jsae.getCmsObject();
CmsAliasBulkEditHelper helper = new CmsAliasBulkEditHelper(cms);
helper.importAliases(request, response);
%>