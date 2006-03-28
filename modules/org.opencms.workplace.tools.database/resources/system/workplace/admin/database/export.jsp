<%@ page import="org.opencms.workplace.tools.database.*" %><%	
	
	CmsDatabaseExportDialog wp = new CmsDatabaseExportDialog(pageContext, request, response);
	wp.displayDialog();
%>