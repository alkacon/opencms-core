<%@ page import="org.opencms.workplace.comparison.*,org.opencms.workplace.list.*" %>
<%
    // initialize and write the widget dialog
    new CmsResourceComparisonDialog(pageContext, request, response).displayDialog();
%>