<%@page import="org.opencms.ade.properties.*" taglibs="c,cms"  %><!DOCTYPE html>
<html>
	<head>
		<%=(new CmsPropertiesActionElement(pageContext, request, response)).exportAll() %>
		<script src="<cms:link>/system/modules/org.opencms.ade.properties/resources/properties/properties.nocache.js</cms:link>"></script>
	</head>
	<body>
	</body>
</html>