<%@ page import="org.opencms.workplace.commons.*" %>
<%
	// initialize the list dialog
	CmsGroupSelectionList wpList = new CmsGroupSelectionList(pageContext, request, response);
	// perform the list actions 
	wpList.displayDialog();
%>
