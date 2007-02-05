<%@ page import="org.opencms.workplace.explorer.CmsNewResourceXmlContentModel" %><% 

    CmsNewResourceXmlContentModel wp = new CmsNewResourceXmlContentModel(pageContext, request, response);
    wp.displayDialog();
%>
