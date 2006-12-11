<%@ page import="org.opencms.workplace.tools.searchindex.*, org.opencms.jsp.CmsJspActionElement, org.opencms.workplace.list.A_CmsListDialog" %>
<% 
  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  CmsOverviewFieldDialog wpWidget = new CmsOverviewFieldDialog(actionElement);

  // perform the widget actions   
  wpWidget.displayDialog(true);
  if (wpWidget.isForwarded()) {
    return;
  }
 
  // Fields list
  A_CmsListDialog listMappings = new CmsMappingsList(actionElement);      
  // perform the list actions 
  listMappings.displayDialog(true);

  // write the content of widget dialog
  wpWidget.writeDialog();
  // write the content of list dialogs
  listMappings.writeDialog();
%>

