<%@ page import="
	org.opencms.workplace.comparison.*
"%><%
    // initialize and write the widget dialog
    new CmsResourceComparisonDialog(pageContext, request, response).displayDialog();
%>