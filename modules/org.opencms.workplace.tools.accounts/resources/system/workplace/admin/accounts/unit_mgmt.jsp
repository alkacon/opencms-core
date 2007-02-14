<%@ page import="org.opencms.workplace.tools.accounts.CmsOrgUnitsSubList, 
                 org.opencms.jsp.CmsJspActionElement" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  CmsOrgUnitsSubList wpOrgUnitsSub = new CmsOrgUnitsSubList(actionElement);
  // perform the list actions   
  wpOrgUnitsSub.displayDialog();
%>