<%@ page import="org.opencms.workplace.commons.CmsHistoryList" %><%	
%><%@ page import="org.opencms.workplace.commons.CmsResourceInfoDialog" %><%	
	// initialize the widget dialog
	CmsResourceInfoDialog wpWidget = new CmsResourceInfoDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize the list dialog
	CmsHistoryList wpList = new CmsHistoryList(wpWidget.getJsp());
	// perform the list actions 
	wpList.displayDialog(true);
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	wpList.writeDialog();
%>