<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateNavigation cms = new CmsTemplateNavigation(pageContext, request, response);

if (cms.template("navrow")) {
	out.print(cms.buildNavigationHead(cms.key("link.home"), "navtop", "navspacer"));
} else {
	out.print(cms.buildNavigationHeadMenus("topmenu"));
}

%>