<%@ page import="org.opencms.workplace.tools.accounts.*"%><%	

	CmsUserRoleDialog wpWidget = new CmsUserRoleDialog(pageContext, request, response);
	wpWidget.displayDialog(true);

	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize the list dialog
	CmsRoleEditList wpList = new CmsRoleEditList(wpWidget.getJsp());

	// perform the list actions 
	wpList.displayDialog(true);

	// write the content of widget dialog
	wpWidget.writeDialog();

	// write the content of list dialog
	wpList.writeDialog();
%>