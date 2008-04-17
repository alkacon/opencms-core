<%@ page import="org.opencms.workplace.tools.cache.*" %><%

	// initialize the widget dialog
	CmsFlexCacheOverviewDialog wpWidget = new CmsFlexCacheOverviewDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize the list dialog
	CmsFlexCacheList wpList = new CmsFlexCacheList(wpWidget.getJsp());
	// perform the list actions 
	wpList.displayDialog(true);
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	wpList.writeDialog();
%>