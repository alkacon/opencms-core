<%@page import="org.opencms.util.CmsMacroFormatterResolver" %><%
CmsMacroFormatterResolver resolver = new CmsMacroFormatterResolver(pageContext, request, response);
resolver.resolve();
%>