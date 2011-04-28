<%@page import="org.opencms.ade.containerpage.CmsContainerpageActionElement"%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsContainerpageActionElement containerpage= new CmsContainerpageActionElement(pageContext, request, response);
%><%= containerpage.exportAll() %>
