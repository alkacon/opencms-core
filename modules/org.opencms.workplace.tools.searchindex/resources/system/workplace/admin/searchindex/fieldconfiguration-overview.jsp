<%@ page import="org.opencms.workplace.tools.searchindex.*, org.opencms.jsp.CmsJspActionElement, org.opencms.workplace.CmsWidgetDialog, org.opencms.workplace.list.A_CmsListDialog" %>
<% 
  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  CmsOverviewFieldConfigurationDialog wpWidget = new CmsOverviewFieldConfigurationDialog(actionElement);

  // perform the widget actions   
  wpWidget.displayDialog(true);
  if (wpWidget.isForwarded()) {
    return;
  }
 
  // Fields list
  A_CmsListDialog listFields = new CmsFieldsList(actionElement);      
  // perform the list actions 
  listFields.displayDialog(true);

  // write the content of widget dialog
  wpWidget.writeDialog();
  // write the content of list dialogs
  listFields.writeDialog();
%>

