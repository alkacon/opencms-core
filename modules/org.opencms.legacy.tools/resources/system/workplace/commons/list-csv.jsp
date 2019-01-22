<%@ page import="org.opencms.workplace.list.*"%>
<%	
	// initialize the workplace class
	CmsListCsvExportDialog wp = new CmsListCsvExportDialog(pageContext, request, response);        
%>
<%= wp.generateCsv() %>
