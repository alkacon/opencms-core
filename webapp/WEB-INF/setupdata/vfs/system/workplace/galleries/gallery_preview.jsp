<%@ page import="org.opencms.jsp.*, org.opencms.workplace.galleries.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	CmsGallery wp = CmsGallery.createInstance(cms);
%>
<%= wp.htmlStart(null) %>
	<link rel="stylesheet" type="text/css" href="<%=wp.getCssPath()%>">
	
<script language="javascript">
<!--
	function reload() {
		self.location.href="<%=wp.getJsp().link(wp.getJsp().getRequestContext().getUri()+"?"+wp.paramsAsRequest())%>";
	}
//-->
</script>	
</head>
<body<%=wp.getPreviewBodyStyle()%>>
<div <%=wp.getPreviewDivStyle()%>>
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
<%= wp.htmlEnd() %>