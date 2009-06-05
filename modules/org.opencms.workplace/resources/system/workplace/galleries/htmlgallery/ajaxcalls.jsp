<%@ page import="org.opencms.workplace.galleries.*" buffer="none" %><%

A_CmsAjaxGallery wp = new CmsAjaxHtmlGallery(pageContext, request, response);

//ajax call of the displayDialog() method of the gallery with the appropriate parameters
wp.displayDialog();

%>