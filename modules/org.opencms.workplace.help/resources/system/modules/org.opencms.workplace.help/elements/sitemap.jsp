<%@ page session="false" import="org.opencms.workplace.help.*,org.opencms.jsp.*" %><%

// get required OpenCms objects
CmsJspActionElement action = new CmsJspLoginBean(pageContext, request, response);
CmsHelpNavigationListView nav = new CmsHelpNavigationListView(action);
nav.setNavigationRootPath( request.getParameter("helpresource"));
nav.setDepth(3);
%><%= nav.createNavigation() %>

