<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryImages wp = new CmsGalleryImages(pageContext, request, response);	
%>
<html>
<head>
<script language="javascript">
<!--
	function reload() {
		self.location.href="<%=wp.getJsp().link(wp.getJsp().getRequestContext().getUri()+"?"+wp.paramsAsRequest())%>";
	}
//-->
</script>	
</head>
<body>
<div style="text-align: center; width: 100%; margin-top: 5px">
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
</html>