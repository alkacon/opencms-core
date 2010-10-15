<%@page buffer="none" session="false" import="org.opencms.frontend.template3.*" %><%--
--%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title><cms:property name="Title" file="search" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" />" >
	<meta name="keywords" content="<cms:property name="Keywords" file="search" />" >
	<meta name="robots" content="index, follow" >
	<meta name="revisit-after" content="7 days" >
	<cms:enable-ade/>
</head>
<body>
	<div id="col1" style="width: 600px; margin: auto; margin-top: 50px;">
		<div id="col1_content">
	  		<cms:container name="centercnt" type="center" maxElements="5" detailview="true" />
	  	</div>
	  </div>
</body>
</html>