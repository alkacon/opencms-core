<%@ page session="false" import="
	org.opencms.jsp.*, 
	org.opencms.workplace.*,
	org.opencms.workplace.galleries.*
"%><%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);

 %><%= wp.htmlStart(null) %>

<style type="text/css">
<%@ include file="gallery.css" %>
</style>
 
<%@ include file="gallery.js" %>

</head>
<body class="buttons-head" unselectable="on">
<form class="nomargin" name="form" action="gallery_buttonbar.jsp" onSubmit="return editProperty('<%=wp.getParamResourcePath()%>');" method="post">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= A_CmsGallery.DIALOG_EDITPROPERTY %>">
<input type="hidden" name="<%= A_CmsGallery.PARAM_GALLERYPATH %>" value="<%= wp.getParamGalleryPath() %>">
<input type="hidden" name="<%= A_CmsGallery.PARAM_RESOURCEPATH %>" value="<%= wp.getParamResourcePath() %>">
<input type="hidden" name="<%= A_CmsGallery.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= A_CmsGallery.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">
<%= wp.buildGalleryButtonBar() %>
</form>
</body>
<%= wp.htmlEnd() %>