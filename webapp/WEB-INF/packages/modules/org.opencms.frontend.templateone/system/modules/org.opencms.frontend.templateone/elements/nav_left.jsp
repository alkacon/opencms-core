<%@page buffer="none" session="false" import="org.opencms.file.*, org.opencms.jsp.*, java.util.*, org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateNavigation cms = new CmsTemplateNavigation(pageContext, request, response);

%><%= cms.buildNavigationLeft() %><% cms.buildNavLeftIncludeElement(); %>