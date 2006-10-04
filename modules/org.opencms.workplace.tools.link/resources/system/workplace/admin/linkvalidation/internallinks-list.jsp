<%@ page import="org.opencms.workplace.tools.link.*"%><%	
    CmsInternalLinkValidationList wp = new CmsInternalLinkValidationList(pageContext, request, response);
    wp.displayDialog();
%>