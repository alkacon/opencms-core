<%@ page import="org.opencms.workplace.tools.content.languagecopy.*" %><%	
	
	CmsLanguageCopyReport wp = new CmsLanguageCopyReport(pageContext, request, response);
	wp.displayReport();
%>