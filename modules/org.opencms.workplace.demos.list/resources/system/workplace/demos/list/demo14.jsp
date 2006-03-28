<%@ page 
	buffer="none" 
	import="org.opencms.workplace.demos.list.*" %>
<%
	// initialize the widget dialog
	CmsGroupDemo wpWidget = new CmsGroupDemo(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize the list dialog
	CmsListDemo14 wpList = new CmsListDemo14(wpWidget.getJsp());
	// perform the list actions 
	wpList.displayDialog(true);
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	wpList.writeDialog();
%>