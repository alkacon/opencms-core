<%@ page import="org.opencms.workplace.tools.content.convertxml.*" %><%	
	
	CmsConvertXmlReport wp = new CmsConvertXmlReport(pageContext, request, response);
	wp.displayReport();
%>