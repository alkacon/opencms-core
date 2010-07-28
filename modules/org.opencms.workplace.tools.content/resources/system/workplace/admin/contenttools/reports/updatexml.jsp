<%@ page import="org.opencms.workplace.tools.content.updatexml.*" %><%	
	
	CmsUpdateXmlReport wp = new CmsUpdateXmlReport(pageContext, request, response);
	wp.displayReport();
%>