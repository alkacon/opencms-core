
<%@page pageEncoding="UTF-8" %>
<%@page import="org.opencms.jsp.CmsJspActionElement,org.opencms.security.CmsRole,org.opencms.workplace.tools.workplace.logging.CmsLog4JAdminDialog" 
%><%
  CmsJspActionElement cmsJsp = new CmsJspActionElement(pageContext, request, response);  
    CmsLog4JAdminDialog wp = new CmsLog4JAdminDialog(cmsJsp);
    wp.displayDialog();    
%>