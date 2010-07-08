<%@page import="org.opencms.ade.galleries.CmsGalleryActionElement, org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
  CmsGalleryActionElement gallery = new CmsGalleryActionElement(pageContext, request, response);
%><!DOCTYPE HTML>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><%= gallery.getTitle() %></title>
    <script type="text/javascript" src="<cms:link>/system/modules/org.opencms.ade.galleries/resources/resources.nocache.js</cms:link>"></script>
    <%= gallery.exportAll() %>
    <% if (gallery.isEditorMode()){ /* opened from rich text editor (FCKEditor, CKEditor...) include necessary scripts */ %>
    <script type="text/javascript" src="<cms:link>/system/workplace/editors/fckeditor/plugins/galleries/integrator.js</cms:link>"></script>
    <% } else if (gallery.isWidgetMode()){ /* opened as widget include necessary scripts */ %>
    <script type="text/javascript">
    	var <%= I_CmsGalleryProviderConstants.KEY_FIELD_ID %> = '<%= request.getParameter(I_CmsGalleryProviderConstants.ReqParam.fieldid.name())%>';
    </script>
    <% } %>
  </head>
  <body>
  </body>
</html>