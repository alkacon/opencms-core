<%@ page import="org.opencms.workplace.tools.database.*" %><%	
	
	CmsDatabaseImportReport wp = new CmsDatabaseImportReport(pageContext, request, response);
	wp.displayReport();
%>

