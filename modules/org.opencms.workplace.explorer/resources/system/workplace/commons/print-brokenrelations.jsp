<%@page import="org.opencms.workplace.commons.CmsDeleteBrokenRelationsList"%>
<%	
   CmsDeleteBrokenRelationsList wp = new CmsDeleteBrokenRelationsList(pageContext, request, response);
   wp.actionDialog();
%><%= wp.generateHtml() %>
