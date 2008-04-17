<%@ page import="org.opencms.workplace.tools.cache.*" %><%

	// initialize the widget dialog
	CmsImageCacheClearDialog wpWidget = new CmsImageCacheClearDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog();
%>