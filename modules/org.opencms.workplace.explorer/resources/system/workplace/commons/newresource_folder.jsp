<%@ page import="org.opencms.workplace.explorer.CmsNewResourceFolder" %><% 

    CmsNewResourceFolder wp = new CmsNewResourceFolder(pageContext, request, response);
    wp.displayDialog();
%>