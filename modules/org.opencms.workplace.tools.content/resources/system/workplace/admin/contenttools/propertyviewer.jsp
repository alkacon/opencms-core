<%@ page import="org.opencms.workplace.tools.content.propertyviewer.*,
		java.util.*,
		org.opencms.workplace.*,
		org.opencms.jsp.*" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  
  CmsWidgetDialog dialog= new CmsPropertyviewDialog(actionElement);
  // perform the list actions   
  dialog.displayDialog(true);

  dialog.writeDialog();
%>