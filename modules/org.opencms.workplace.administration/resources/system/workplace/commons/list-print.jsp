<%@ page import="org.opencms.workplace.list.*"%><%	

	CmsListPrintDialog wp = new CmsListPrintDialog(pageContext, request, response);        

%><%= wp.generateHtml() %>