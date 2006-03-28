<%@ page import="org.opencms.workplace.tools.database.*" %><%	
	
	CmsDatabaseExportReport wp = new CmsDatabaseExportReport(pageContext, request, response);
	wp.displayReport();
%>

