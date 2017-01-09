<%@page import="org.opencms.ade.properties.*" %><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html>
	<head>
		<%=(new CmsPropertiesActionElement(pageContext, request, response)).exportAll() %>
	</head>
	<body>
	</body>
</html>