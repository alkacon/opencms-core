<%@ page import="org.opencms.workplace.commons.*" %>
<%
	// initialize the list dialog
	CmsUserSelectionList wpList = new CmsUserSelectionList(pageContext, request, response);
	// perform the list actions 
	wpList.displayDialog();
%>
