<%@ page import="org.opencms.workplace.tools.accounts.*"  %><%@ page import="org.opencms.main.*" %><%

	// initialize info dialog
	CmsRoleOverviewDialog wpInfo = new CmsRoleOverviewDialog(pageContext, request, response);
	// perform the widget actions
	wpInfo.displayDialog(true);
	if (wpInfo.isForwarded()) {
		return;
	}
	// initialize list dialogs
	
	CmsShowRoleUsersList wpRoleUsers = new CmsShowRoleUsersList(pageContext, request, response, OpenCms.getWorkplaceManager().supportsLazyUserLists());
	// perform the active list actions
	wpRoleUsers.displayDialog(true);
	// write the content of widget dialog
	wpInfo.writeDialog();
	// write the content of list dialogs
	wpRoleUsers.writeDialog();   
%>
