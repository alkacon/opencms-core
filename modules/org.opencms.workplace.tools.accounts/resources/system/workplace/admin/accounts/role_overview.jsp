<%@ page import="org.opencms.workplace.tools.accounts.*"  %><%

	// initialize info dialog
	CmsRoleOverviewDialog wpInfo = new CmsRoleOverviewDialog(pageContext, request, response);
	// perform the widget actions
	wpInfo.displayDialog(true);
	if (wpInfo.isForwarded()) {
		return;
	}
	// initialize list dialogs
	
	CmsShowRoleUsersList wpRoleUsers = new CmsShowRoleUsersList(pageContext, request, response);
	// perform the active list actions
	wpRoleUsers.displayDialog(true);
	// write the content of widget dialog
	wpInfo.writeDialog();
	// write the content of list dialogs
	wpRoleUsers.writeDialog();   
%>
