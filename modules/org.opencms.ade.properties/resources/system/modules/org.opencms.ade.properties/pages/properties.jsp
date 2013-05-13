<%@page import="org.opencms.ade.properties.*" taglibs="c,cms"  %><!DOCTYPE html>
<html>
	<head>
		<%=(new CmsPropertiesActionElement(pageContext, request, response)).exportAll() %>
	</head>
	<body>
	</body>
</html>
