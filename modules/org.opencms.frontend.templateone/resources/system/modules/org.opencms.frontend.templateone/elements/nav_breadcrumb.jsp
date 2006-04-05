<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateNavigation cms = new CmsTemplateNavigation(pageContext, request, response);

%><%= cms.buildNavigationBreadCrumb("breadcrumb") %>