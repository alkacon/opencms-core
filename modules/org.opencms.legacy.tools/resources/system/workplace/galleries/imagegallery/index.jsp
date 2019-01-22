<%@ page import="org.opencms.gwt.shared.I_CmsUploadConstants,org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*,org.opencms.ade.upload.CmsUploadActionElement" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

String galleryResourcePath = org.opencms.workplace.CmsWorkplace.getSkinUri() + "components/galleries/";
String jQueryResourcePath = org.opencms.workplace.CmsWorkplace.getSkinUri() + "jquery/";
String jsIntegratorQuery = "";

//check in settings if the upload-applet is used
String uploadVariant = wp.getSettings().getUserSettings().getUploadVariant().toString();


%><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>

<title><%= wp.key(Messages.GUI_TITLE_IMAGEGALLERY_0) %></title>
<link rel="stylesheet" type="text/css" href="<%= galleryResourcePath %>css/dialog.css" />
<link rel="stylesheet" type="text/css" href="<%= jQueryResourcePath %>css/thickbox/thickbox.css" />
<link rel="stylesheet" type="text/css" href="<%= jQueryResourcePath %>css/ui-ocms/jquery.ui.css" />
<link rel="stylesheet" type="text/css" href="<%= jQueryResourcePath %>css/ui-ocms/jquery.ui.ocms.css" />

<% if (wp.isModeEditor()) { %>
<link rel="stylesheet" type="text/css" href="<%= galleryResourcePath %>css/editor.css" />
<% } %>
<!--[if lte IE 7]>
  <link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/galleries/css/ie.css" />
<![endif]-->

<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.pagination.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/thickbox.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.ui.js"></script>

<script type="text/javascript" src="<%= wp.getJsp().link("../galleryelements/localization.js?locale=" + wp.getLocale()) %>"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/jquery.jeditable.pack.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/jquery.jHelperTip.1.0.min.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/formatselections.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/galleryfunctions.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/imagegallery/imagegalleryfunctions.js"></script>

<script type="text/javascript">

var vfsPathAjaxJsp = "<%= wp.getJsp().link("/system/workplace/galleries/imagegallery/ajaxcalls.jsp") %>";
var vfsPathPrefixItems = "<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/galleries/img/";
var vfsPathButtons = "<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/";
var initValues;

<% 
if (wp.isModeView()) {
	wp.getJsp().includeSilent("../galleryelements/integrator_view.js", null);
} else {
	String variant = "";
	if (request.getParameter("integrator") != null) {
		wp.getJsp().includeSilent(request.getParameter("integrator"), null);
	} else {
		wp.getJsp().includeSilent("js/integrator_" + wp.getParamDialogMode() + variant + ".js", null);
	}
}
%> 

var uploadVariant = '<%=uploadVariant %>';

</script>

<%
	if (uploadVariant.equals("gwt")) {
	    CmsUploadActionElement upload = new CmsUploadActionElement(pageContext, request, response);
	    %>
	    	<%= upload.exportButton() %>
	    <%
	}
%>
</head>
<body id="gallerydialog">

	<!-- Tabs with jQuery -->
    	<div id="tabs">
		<%@ include file="%(link.strong:/system/workplace/galleries/galleryelements/gallerytabs_imagegallery.html:9746bf0a-28d3-11de-91af-a75c4a166a24)" %>
		
		<%
		if (wp.isModeWidget()) { %>
			<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tabs_widget_imagegallery.html:bb9bf84b-a42c-11dd-a77f-55b439b85a0e)" %><%
		} else if (wp.isModeEditor()) { %>
			<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tabs_editor_imagegallery.html:453c7b4e-a430-11dd-a77f-55b439b85a0e)" %><%
		}
		%>
	
		<!-- Galleries-Tab: in all modes, the only tab in view mode -->
		<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tab_galleries_imagegallery.jsp:238cabe9-359e-11de-8f50-a75c4a166a24)" %>
	
		<%
		if (wp.isModeWidget() || wp.isModeEditor()) { %>
			<!-- Categories-Tab: in editor and widget mode --> 
			<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tab_categories_imagegallery.jsp:3f9c344d-359e-11de-8f50-a75c4a166a24)" %><%
		}
		%>
	</div> <!-- close tag for tabs -->
	<div id="closebutton">
		<button type="button" onclick="window.close();"><%= wp.key(Messages.GUI_GALLERY_BUTTON_CLOSE_0) %></button>
	</div>
	<!-- The dialog html for the search dialog -->
	<%@ include file="%(link.strong:/system/workplace/galleries/galleryelements/searchdialog.html:01f57eeb-5f28-11de-8c07-2d12956623b5)" %>
	<a href="#" class="thickbox" id="resourcepublishlink"></a>
	<a href="#" class="thickbox" id="resourcedeletelink"></a>
</body>

</html>