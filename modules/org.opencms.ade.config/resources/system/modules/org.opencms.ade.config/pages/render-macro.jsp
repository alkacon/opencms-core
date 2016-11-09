<%@page import="org.opencms.jsp.util.CmsMacroFormatterResolver" session="false" %><%
CmsMacroFormatterResolver resolver = new CmsMacroFormatterResolver(pageContext, request, response);
resolver.resolve();
%>