<%@ page import="org.opencms.workplace.tools.workplace.broadcast.*" %><% 

    CmsSendEmailGroupsDialog wp = new CmsSendEmailGroupsDialog(pageContext, request, response);
    wp.displayDialog();
%>