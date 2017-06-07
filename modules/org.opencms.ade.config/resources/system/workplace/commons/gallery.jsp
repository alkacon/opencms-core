<%@page import="org.opencms.ade.galleries.CmsGalleryActionElement, org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants, org.opencms.util.CmsStringUtil, org.opencms.i18n.CmsEncoder" %><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%
  CmsGalleryActionElement gallery = new CmsGalleryActionElement(pageContext, request, response);
%><!DOCTYPE html>
<html>
  <head>
  	<title><%= gallery.getTitle() %></title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  	<c:if test="${!empty param.integrator}">
	<c:set var="integrator"><%= CmsEncoder.escapeXml(request.getParameter("integrator")) %></c:set>
	<script type="text/javascript" src="<cms:link>${integrator}</cms:link>?integratorArgs=${param.integratorArgs}"></script>
	</c:if>
    
    <%= gallery.exportAll() %>
    <style type="text/css">
    	* { 
    		zoom: 1; 
    	}
    </style>
    <% if (gallery.isWidgetMode()){ /* opened as widget include necessary scripts */ %>
    <script type="text/javascript">
    	var <%= I_CmsGalleryProviderConstants.KEY_FIELD_ID %> = '<%= CmsStringUtil.escapeJavaScript(request.getParameter(I_CmsGalleryProviderConstants.KEY_FIELD_ID)) %>';
    	var <%= I_CmsGalleryProviderConstants.KEY_HASH_ID %> = '<%= CmsStringUtil.escapeJavaScript(request.getParameter(I_CmsGalleryProviderConstants.KEY_HASH_ID)) %>';
    	function closeDialog(){
    	    window.parent.cmsCloseDialog(<%= I_CmsGalleryProviderConstants.KEY_FIELD_ID %>);
    	}
    </script>
    <% } %>
  </head>
  <body>
  </body>
</html>