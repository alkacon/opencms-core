<%@ page import="org.opencms.workplace.tools.searchindex.*, org.opencms.jsp.CmsJspActionElement, org.opencms.workplace.CmsWidgetDialog, org.opencms.workplace.list.A_CmsListDialog" %>
<% 
  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  CmsWidgetDialog wpWidget = new CmsOverviewIndexSourceDialog(actionElement);

  // perform the widget actions   
  wpWidget.displayDialog(true);
  if (wpWidget.isForwarded()) {
    return;
  }
 
  // Document types list
  A_CmsListDialog listDoctypes = new CmsDocumentTypeList(actionElement);      
  // perform the list actions 
  listDoctypes.displayDialog(true);

  // Search resources list
  A_CmsListDialog listResources = new CmsSearchResourcesList(actionElement);      
  // perform the list actions 
  listResources.displayDialog(true);

  // write the content of widget dialog
  wpWidget.writeDialog();
  // write the content of list dialogs
  listDoctypes.writeDialog();
  listResources.writeDialog();
%>

