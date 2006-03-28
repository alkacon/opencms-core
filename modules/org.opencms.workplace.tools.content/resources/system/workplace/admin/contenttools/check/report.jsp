<%@ page import="org.opencms.workplace.tools.content.check.*" %><%	
	
	CmsContentCheckReport wp = new CmsContentCheckReport(pageContext, request, response);
	wp.displayReport();
%>