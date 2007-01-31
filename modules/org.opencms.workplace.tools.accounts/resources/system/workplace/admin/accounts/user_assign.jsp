<%@ page import="org.opencms.workplace.tools.accounts.*"  %><%

	// initialize list dialogs
	CmsOrgUnitUsersList wpOrgUnitUsers = new CmsOrgUnitUsersList(pageContext, request, response);
	CmsNotOrgUnitUsersList wpNotOrgUnitUsers = new CmsNotOrgUnitUsersList(pageContext, request, response);
	
	CmsTwoOrgUnitUsersList wpTwoLists = new CmsTwoOrgUnitUsersList(wpOrgUnitUsers, wpNotOrgUnitUsers);
	// perform the active list actions
	wpTwoLists.displayDialog();   
%>