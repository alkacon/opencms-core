<%@ page session="false" import="org.opencms.workplace.editors.sitemap.CmsSitemapActionElement, org.opencms.workplace.CmsWorkplace" %>
<%@ page import="org.opencms.main.*" %>

<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsSitemapActionElement jsp = new CmsSitemapActionElement(pageContext, request, response);
  pageContext.setAttribute("cms", jsp);
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/lib/jquery-1.3.2.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/lib/jquery-ui-1.8a1.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/lib/json2.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/lib/jquery.jHelperTip.1.0.js")%>" ></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.directinput.js")%>"></script>
    <script type="text/javascript">
      var cms = {
         data: {
            "EDITOR_URL": "<cms:link>%(link.weak:/system/workplace/editors/editor.jsp:8973b380-11b7-11db-91cd-fdbae480bac9)</cms:link>",
            "SERVER_URL": "<cms:link>%(link.weak:/system/workplace/editors/sitemap/server.jsp:b848e330-c865-11de-a457-ab20365f6268)</cms:link>",
            "LOCALE": "${cms.requestContext.locale}",
            "SITEMAP_URI": "${cms.sitemapURI}",
            "CONTEXT": "<%=OpenCms.getSystemInfo().getOpenCmsContext()%>",
            "SKIN_URI"	: "%(skinUri)"
         },
         publish: {
            "SERVER_URL": "<cms:link>%(link.weak:/system/workplace/editors/sitemap/publish-server.jsp:dd962f3e-abcd-11de-97fc-dd9f629b113b)</cms:link>"
         },
         html: {},
         comm: {},
         messages: {},
         sitemap: {},
         util: {},
         property: {}
      };
    </script>
    <script type="text/javascript" src="<cms:link>%(link.weak:/system/workplace/editors/sitemap/cms.messages.jsp:b829e977-c865-11de-a457-ab20365f6268)</cms:link>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.comm.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.html.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.data.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.publish.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.property.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.util.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.sitemap.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/ade/js/cms.selectbox.js")%>"></script>
    <script type="text/javascript">
      $(document).ready(function() {
         cms.data.loadAndInitSitemap();
      });
    </script>
    <title>Sitemap demo</title>
    <link rel="stylesheet" type="text/css" media="screen" href="<%=CmsWorkplace.getResourceUri("editors/ade/css/custom-theme/jquery-ui-1.7.2.custom.css")%>" />
    <link rel="stylesheet" type="text/css" media="screen" href="<%=CmsWorkplace.getResourceUri("editors/ade/css/advanced_direct_edit.css")%>" />
    <link rel="stylesheet" type="text/css" media="screen" href="<%=CmsWorkplace.getResourceUri("editors/sitemap/css/sitemap.css")%>" />
  </head>
  <body>
    <div id="cms-main">
      <h1 class="cms-headline">Sitemap-Editor</h1>
      <div class="cms-box ui-corner-all">
        <ul id="cms-sitemap"></ul>
        <div class="cms-clearer"></div>
      </div>
    </div>
  </body>
</html>
