<%@ page import="org.opencms.workplace.tools.accounts.CmsUsersAllOrgUnitsList" %><%@page import="org.opencms.main.*" %><% 

    CmsUsersAllOrgUnitsList wp = new CmsUsersAllOrgUnitsList(pageContext, request, response, OpenCms.getWorkplaceManager().supportsLazyUserLists());
    wp.displayDialog();
%>