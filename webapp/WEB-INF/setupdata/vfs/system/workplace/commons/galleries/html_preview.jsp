<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryHtmls wp = new CmsGalleryHtmls(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
<script language="javascript">
<!--
	function reload() {
		self.location.href="<%=wp.getJsp().link(wp.getJsp().getRequestContext().getUri()+"?"+wp.paramsAsRequest())%>";
	}
//-->
</script>		
</head>
<body class="dialog" height="100%" unselectable="on">
<div width: 100%; margin-top: 5px">
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
<%= wp.htmlEnd() %>