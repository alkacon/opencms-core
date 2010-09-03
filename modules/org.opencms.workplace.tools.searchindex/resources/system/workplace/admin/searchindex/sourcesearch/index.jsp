<%@ page import="org.opencms.workplace.tools.searchindex.sourcesearch.*,
		java.util.*,
		org.opencms.workplace.*,
		org.opencms.jsp.*" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  
  CmsWidgetDialog dialog = new CmsSourceSearchDialog(actionElement);
  // perform the list actions   
  dialog.displayDialog(true);
  dialog.writeDialog();
%>