<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
	<%@ include file="gallery.js" %>
</head>
<body class="dialog" style="background-color: ThreeDFace;" height="100%" unselectable="on">
<form class="nomargin" name="form" action="link_buttonbar.jsp">
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