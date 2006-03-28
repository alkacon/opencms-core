<%@ page import="org.opencms.workplace.tools.database.*" %><%	
	
	CmsDatabaseImportFromHttp wp = new CmsDatabaseImportFromHttp(pageContext, request, response);
	wp.displayDialog();
%>