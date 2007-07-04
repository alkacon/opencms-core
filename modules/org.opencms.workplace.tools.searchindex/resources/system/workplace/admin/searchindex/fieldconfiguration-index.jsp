<%@ page import="org.opencms.workplace.tools.searchindex.*, org.opencms.workplace.list.*" %>
<% 
    A_CmsListDialog wp = new CmsSearchFieldConfigurationList(pageContext, request, response);
    wp.displayDialog();
%>

