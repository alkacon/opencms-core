<%@ page import="org.opencms.jsp.*, org.opencms.workplace.galleries.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);
%>
<%= wp.htmlStart(null) %>
	<%@ include file="gallery.js" %>
</head>
<body class="buttons-head" unselectable="on">
<form class="nomargin" name="form" action="gallery_buttonbar.jsp" onSubmit="return editProperty('<%=wp.getParamResourcePath()%>');">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.ACTION_EDITPROPERTY %>">
<input type="hidden" name="<%= wp.PARAM_GALLERYPATH %>" value="<%= wp.getParamGalleryPath() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCEPATH %>" value="<%= wp.getParamResourcePath() %>">
<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">
<%= wp.buildGalleryButtonBar() %>
</form>
</body>
<%= wp.htmlEnd() %>