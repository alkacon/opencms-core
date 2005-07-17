<%@ page session="false" import="org.opencms.workplace.help.*,org.opencms.jsp.*" %><%

// get required OpenCms objects
CmsJspActionElement action = new CmsJspLoginBean(pageContext, request, response);
%>

<jsp:useBean id="search" scope="request" class="org.opencms.search.CmsSearch">
<jsp:setProperty name="search" property="*"/>
</jsp:useBean>
<%
CmsHelpSearchResultView searchView = new CmsHelpSearchResultView(action);
// online help template specific: 
StringBuffer paramHelpresource = new StringBuffer("/system/workplace/locales/");
paramHelpresource.append(action.getRequestContext().getLocale().getLanguage());
paramHelpresource.append("/help/search.html");
searchView.setSearchRessourceUrl(action.link(paramHelpresource.toString()));

%><%= searchView.displaySearchResult(search)%>


