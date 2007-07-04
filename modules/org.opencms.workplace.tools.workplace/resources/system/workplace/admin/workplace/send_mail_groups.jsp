<%@ page import="org.opencms.workplace.tools.workplace.broadcast.*" %><% 

    CmsSelectReceiverDialog wp = new CmsSelectReceiverDialog(pageContext, request, response);
    wp.displayDialog();
%>