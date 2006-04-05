<%@ page session="false" import="org.opencms.workplace.help.*,org.opencms.jsp.*" %><%

// get required OpenCms objects
CmsJspActionElement action = new CmsJspLoginBean(pageContext, request, response);

// use template html:
CmsHelpTemplateBean template = new CmsHelpTemplateBean(pageContext,request, response);
%>
<%=template.buildHtmlHelpStart("onlinehelp.css", true) 
%><body style="padding:12px;">
<jsp:useBean id="search" scope="request" class="org.opencms.search.CmsSearch">
<jsp:setProperty name="search" property="*"/>
</jsp:useBean>
<%
CmsHelpSearchResultView searchView = new CmsHelpSearchResultView(action);
// online help template specific: 
searchView.setExportLinks(true);
%>
<%= searchView.displaySearchResult(search)%>
<%= template.buildHtmlHelpEnd() %>
