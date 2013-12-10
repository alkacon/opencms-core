<%@page taglibs="c"  import="org.opencms.ade.galleries.CmsGalleryActionElement, org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants, org.opencms.util.CmsStringUtil, org.opencms.i18n.CmsEncoder" taglibs="cms"%><%
  CmsGalleryActionElement gallery = new CmsGalleryActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
  <head>
  	<title><%= gallery.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  	<c:choose>
	<c:when test="${!empty param.integrator}">
	<c:set var="integrator"><%= CmsEncoder.escapeXml(request.getParameter("integrator")) %></c:set>
	<script type="text/javascript" src="<cms:link>${integrator}</cms:link>?integratorArgs=${param.integratorArgs}"></script>
	</c:when>
	<c:otherwise>    
    <script type="text/javascript" src="<cms:link>/system/workplace/editors/fckeditor/plugins/galleries/integrator.js</cms:link>"></script>
    </c:otherwise>
    </c:choose>
    
    <%= gallery.exportAll() %>
    <style type="text/css">
    	* { 
    		zoom: 1; 
    	}
    </style>
    <% if (gallery.isEditorMode()){ /* opened from rich text editor (FCKEditor, CKEditor...) include necessary scripts */ %>
    <% } else if (gallery.isWidgetMode()){ /* opened as widget include necessary scripts */ %>
    <script type="text/javascript">
    	var <%= I_CmsGalleryProviderConstants.KEY_FIELD_ID %> = '<%= CmsStringUtil.escapeJavaScript(request.getParameter(I_CmsGalleryProviderConstants.KEY_FIELD_ID)) %>';
    	var <%= I_CmsGalleryProviderConstants.KEY_HASH_ID %> = '<%= CmsStringUtil.escapeJavaScript(request.getParameter(I_CmsGalleryProviderConstants.KEY_HASH_ID)) %>';
    </script>
    <% } %>
  </head>
  <body>
  	<div id="<%= I_CmsGalleryProviderConstants.GALLERY_DIALOG_ID %>"></div>
  </body>
</html>