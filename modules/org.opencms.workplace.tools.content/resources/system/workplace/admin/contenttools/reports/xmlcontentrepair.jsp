<%@ page import="org.opencms.workplace.tools.content.*" %><%	
	
	CmsXmlContentRepairReport wp = new CmsXmlContentRepairReport(pageContext, request, response);
	wp.displayReport();
%>