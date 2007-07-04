<%@ page import="org.opencms.jsp.*, org.opencms.workplace.galleries.*" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);

	boolean useCss = wp.getGalleryTypeName().startsWith("html") || wp.getGalleryTypeName().startsWith("table");

 %><%= wp.htmlStart(null) %>

<link rel="stylesheet" type="text/css" href="<%=wp.getCssPath()%>">
<script type="text/javascript" language="javascript">
<!--
	if (<%= useCss %> && top.cssPath != "") {
		document.write('<'+'link type="text/css" rel="stylesheet" href="' + top.cssPath + '">');
	}

	function reload() {
		self.location.href="<%=wp.getJsp().link(wp.getJsp().getRequestContext().getUri()+"?"+wp.paramsAsRequest())%>";
	}
	document.onmousemove = new Function('return false;');
//-->
</script>	
</head>
<body<%=wp.getPreviewBodyStyle()%>>
<div <%=wp.getPreviewDivStyle()%>>
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
<%= wp.htmlEnd() %>