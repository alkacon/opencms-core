<%@ page import="org.opencms.workplace.tools.accounts.CmsOrgUnitsAdminList,
                 org.opencms.jsp.CmsJspActionElement,
                 org.opencms.util.CmsRequestUtil,
                 org.opencms.main.OpenCms,
                 java.util.HashMap,
                 java.util.Map" %><% 

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
  
  CmsOrgUnitsAdminList wpOrgUnitsAdmin = new CmsOrgUnitsAdminList(actionElement);
  // perform the list actions   
  wpOrgUnitsAdmin.displayDialog(true);

  if(!wpOrgUnitsAdmin.hasMoreAdminOUs()){
    wpOrgUnitsAdmin.forwardToSingleAdminOU();
    return;
  }

  // write the content of list dialogs
  wpOrgUnitsAdmin.writeDialog();
%>