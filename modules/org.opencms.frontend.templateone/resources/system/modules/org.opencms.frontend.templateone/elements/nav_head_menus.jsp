<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateNavigation cms = new CmsTemplateNavigation(pageContext, request, response);

// set the default property value to create head navigation menu items
// true: all folders and files without the property "style_head_nav_showitem" explicitly set will be shown in head navigation menu
// false: no folder or file without the property "style_head_nav_showitem" explicitly set will be shown in head navigation menu
cms.setHeadNavItemDefaultValue(true);

out.print(cms.buildNavigationHeadMenus("menu"));

%>