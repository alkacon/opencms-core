<%@page buffer="none" session="false" import="org.opencms.workplace.editors.ade.CmsADEPublishServer" %><%

  CmsADEPublishServer ade = new CmsADEPublishServer(pageContext, request, response);
  ade.serve();
%>