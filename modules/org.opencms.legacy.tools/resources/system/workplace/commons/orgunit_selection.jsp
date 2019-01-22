<%@ page import="org.opencms.workplace.commons.*" %>
<%
	// initialize the list dialog
	CmsOrgUnitSelectionList wpList = new CmsOrgUnitSelectionList(pageContext, request, response);
	// perform the list actions 
	wpList.displayDialog();
%>
