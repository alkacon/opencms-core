<%@ page import="org.opencms.workplace.tools.searchindex.*, org.opencms.workplace.CmsWidgetDialog" %>
<% 
    CmsWidgetDialog wp = new CmsDeleteFieldConfigurationDialog(pageContext, request, response);
    wp.displayDialog();
%>

