<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryImages wp = new CmsGalleryImages(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
	<style type="text/css">
	<!--
		td.list { white-space: nowrap; padding-left: 2px; }
		
		td.headline { padding: 1px; white-space: nowrap; background:Menu; border-right: 1px solid ThreedDarkShadow; border-top: 1px solid ThreeDHighlight; border-bottom: 1px solid ThreedDarkShadow; border-left: 1px solid ThreeDHighlight; }
	//-->
	</style>	
	<%@ include file="gallery.js" %>
</head>
<body class="dialog" style="background-color: <%=wp.getParamResourcePath()==null?"#FFF":"ThreeDFace;"%>" height="100%" unselectable="on">
<form class="nomargin" name="form" action="img_buttonbar.jsp">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.ACTION_EDITPROPERTY %>">
<input type="hidden" name="<%= wp.PARAM_PROPERTYVALUE %>" value="">
<input type="hidden" name="<%= wp.PARAM_GALLERYPATH %>" value="<%= wp.getParamGalleryPath() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCEPATH %>" value="<%= wp.getParamResourcePath() %>">
<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">

<%= wp.buildGalleryButtonBar() %>
</form>
</body>
<%= wp.htmlEnd() %>