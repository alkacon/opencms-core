<%@ page import="org.opencms.workplace.tools.cache.*" %><%

	// initialize the widget dialog
	CmsFlexCacheClearDialog wpWidget = new CmsFlexCacheClearDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog();
%>