<%@ page import="org.opencms.workplace.comparison.CmsResourceComparisonDialog" %><%	
%><%@ page import="org.opencms.workplace.commons.CmsResourceInfoDialog" %><%	

	// initialize the widget dialog
	CmsResourceInfoDialog wpWidget = new CmsResourceInfoDialog(pageContext, request, response);
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