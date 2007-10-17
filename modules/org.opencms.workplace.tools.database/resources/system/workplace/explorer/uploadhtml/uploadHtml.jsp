<%@ page import="org.opencms.workplace.tools.database.*" %><%	
	// initialize the widget dialog
	CmsNewResourceUploadHtml wp = new CmsNewResourceUploadHtml(pageContext, request, response);
	//show the dialog 
	wp.displayDialog();

%>