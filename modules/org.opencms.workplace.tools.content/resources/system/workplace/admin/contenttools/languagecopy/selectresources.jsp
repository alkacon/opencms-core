<%@ page import="org.opencms.workplace.tools.content.languagecopy.*,
		java.util.*,
		org.opencms.workplace.*,
		org.opencms.workplace.list.*,
		org.opencms.jsp.*" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  
  A_CmsListDialog list = new CmsLanguageCopySelectionList(actionElement);
  // perform the list actions   
  list.displayDialog(true);

  list.writeDialog();
%>
