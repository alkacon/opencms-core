<%@ page import="org.opencms.workplace.list.*"%>
<%	
	// initialize the workplace class
	CmsListCsvExportDialog wp = new CmsListCsvExportDialog(pageContext, request, response);    
	org.opencms.flex.CmsFlexController.getController(request).getTopResponse().setContentType("text/csv");
%>
<%= wp.generateCsv() %>
