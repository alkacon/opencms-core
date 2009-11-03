<%@page buffer="none" session="false" import="org.opencms.workplace.editors.ade.CmsADEServer" %><%

  CmsADEServer ade = new CmsADEServer(pageContext, request, response);
  ade.serve();
%>