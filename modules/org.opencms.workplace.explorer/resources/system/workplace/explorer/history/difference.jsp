<%@ page import="org.opencms.workplace.comparison.*" %><%	

	// initialize the widget dialog
	CmsFileInfoDialog wpWidget = new CmsFileInfoDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	new CmsResourceComparisonDialog(wpWidget.getJsp()).displayDifferenceDialog();
%>