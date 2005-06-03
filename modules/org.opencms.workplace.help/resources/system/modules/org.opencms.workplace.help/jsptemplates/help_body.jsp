<%@ page session="false" buffer="none" import="org.opencms.workplace.help.*" %><%

// Create a JSP template bean
CmsHelpTemplateBean wp = new CmsHelpTemplateBean(pageContext, request, response);


%><%= wp.displayBody() %>