<%@ page import="org.opencms.workplace.commons.CmsDelete" %><%	

	CmsDelete wp = new CmsDelete(pageContext, request, response);
%><%= wp.printBrokenRelations() %>

