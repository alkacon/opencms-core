<%@page buffer="none" session="false" import="org.opencms.workplace.editors.ade.CmsADEManager" %><%

  CmsADEManager ade = new CmsADEManager(pageContext, request, response);
  ade.serve();
%>