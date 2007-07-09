<%@ page import="org.opencms.workplace.commons.*" %><%	
%><%@ page import="org.opencms.workplace.list.CmsTwoListsDialog" %><%	
	// initialize the widget dialog
	CmsResourceInfoDialog wpWidget = new CmsResourceInfoDialog(pageContext, request, response);
	// perform the widget actions   
	wpWidget.displayDialog(true);
	if (wpWidget.isForwarded()) {
		return;
	}
	// initialize list dialogs
	CmsResourceCategoriesList wpResourceCategories = new CmsResourceCategoriesList(pageContext, request, response);
	CmsNotResourceCategoriesList wpNotResourceCategories = new CmsNotResourceCategoriesList(pageContext, request, response);
	CmsTwoListsDialog wpTwoLists = new CmsTwoListsDialog(wpResourceCategories, wpNotResourceCategories);
	// perform the active list actions
	wpTwoLists.displayDialog(true);
	// write the content of widget dialog
	wpWidget.writeDialog();
	// write the content of list dialogs
	wpTwoLists.writeDialog();
%>