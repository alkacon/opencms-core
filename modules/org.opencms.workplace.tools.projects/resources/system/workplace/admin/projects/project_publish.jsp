<%@ page import="org.opencms.workplace.tools.projects.*" %><%	
	
	CmsPublishProjectReport wp = new CmsPublishProjectReport(pageContext, request, response);
	wp.displayReport();
%>