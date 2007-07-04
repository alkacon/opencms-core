<%@ page import="
	org.opencms.workplace.tools.accounts.CmsOrgUnitOverviewDialog, 
	org.opencms.workplace.tools.accounts.CmsShowOrgUnitResourceList"%>
<%
	// initialize the widget dialog
	CmsOrgUnitOverviewDialog wpWidget = new CmsOrgUnitOverviewDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}

	// initialize the list dialog
	CmsShowOrgUnitResourceList wpList = new CmsShowOrgUnitResourceList(wpWidget.getJsp());
	// perform the list actions 
	wpList.displayDialog(true);

	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialog
	wpList.writeDialog();
%>