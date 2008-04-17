<%@ page import="org.opencms.workplace.tools.cache.*" %><%

	// initialize the widget dialog
	CmsImageCacheOverviewDialog wpWidget = new CmsImageCacheOverviewDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize the list dialog
	CmsImageCacheList wpList = new CmsImageCacheList(wpWidget.getJsp());
	// perform the list actions 
	wpList.displayDialog(true);
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	wpList.writeDialog();
%>