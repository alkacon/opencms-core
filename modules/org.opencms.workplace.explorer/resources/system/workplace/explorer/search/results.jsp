<%@ page import="org.opencms.workplace.search.*" %>
<%
	// initialize the list dialog
	CmsSearchResultsList wp = new CmsSearchResultsList(pageContext, request, response);
	// write the content of list dialog
	wp.displayDialog();
%>