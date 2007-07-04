<%@ page import="org.opencms.workplace.commons.*, org.opencms.jsp.*"  %><%

	// initialize list dialogs
	CmsShowSiblingsList wp = new CmsShowSiblingsList(new CmsJspActionElement(pageContext, request, response));
	// perform the active list actions
	wp.displayDialog(true);
	// write the content of list dialogs
	wp.writeDialog();   
%>