<%@ page import="org.opencms.workplace.commons.*"  %><%

	// initialize list dialogs
	CmsResourceCategoriesList wpResourceCategories = new CmsResourceCategoriesList(pageContext, request, response);
	CmsNotResourceCategoriesList wpNotResourceCategories = new CmsNotResourceCategoriesList(pageContext, request, response);
	org.opencms.workplace.list.CmsTwoListsDialog wpTwoLists = new org.opencms.workplace.list.CmsTwoListsDialog(wpResourceCategories, wpNotResourceCategories);
	// perform the active list actions
	wpTwoLists.displayDialog(true);
	// write the content of list dialogs
	wpTwoLists.writeDialog();   
%>