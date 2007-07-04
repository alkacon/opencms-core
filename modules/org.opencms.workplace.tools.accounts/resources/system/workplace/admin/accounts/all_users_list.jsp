<%@ page import="org.opencms.workplace.tools.accounts.CmsUsersAllOrgUnitsList" %><% 

    CmsUsersAllOrgUnitsList wp = new CmsUsersAllOrgUnitsList(pageContext, request, response);
    wp.displayDialog();
%>