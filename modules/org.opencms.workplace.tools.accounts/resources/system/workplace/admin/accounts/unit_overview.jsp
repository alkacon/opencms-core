<%@ page import="org.opencms.workplace.tools.accounts.CmsOrgUnitOverviewDialog"%><%

	// initialize the widget dialog
	CmsOrgUnitOverviewDialog wpWidget = new CmsOrgUnitOverviewDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// write the content of widget dialog
	wpWidget.writeDialog();
%>