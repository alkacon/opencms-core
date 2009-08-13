<%@ page import="org.opencms.workplace.tools.content.propertyviewer.*,
		java.util.*,
		org.opencms.jsp.*" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  
  CmsPropertyviewList list = new CmsPropertyviewList(actionElement);
  // perform the list actions   
  list.displayDialog(true);

  list.writeDialog();
%>