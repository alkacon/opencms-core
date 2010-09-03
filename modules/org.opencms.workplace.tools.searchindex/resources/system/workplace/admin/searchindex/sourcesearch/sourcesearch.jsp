<%@ page import="org.opencms.workplace.tools.searchindex.sourcesearch.*" %>
<%  
  CmsSourceSearchReport wp = new CmsSourceSearchReport(pageContext, request, response);
  wp.displayReport();
%>