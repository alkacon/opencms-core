<%@ page import="org.opencms.workplace.commons.*" %>
<%
	// initialize the list dialog
	CmsRoleSelectionList wpList = new CmsRoleSelectionList(pageContext, request, response);
	// perform the list actions 
	wpList.displayDialog();
%>
