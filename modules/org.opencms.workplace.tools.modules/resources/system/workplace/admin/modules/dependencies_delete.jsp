<%@ page import="org.opencms.workplace.tools.modules.*" %><%	
	
	CmsDependenciesDelete wp = new CmsDependenciesDelete(pageContext, request, response);
	wp.displayDialog();
%>