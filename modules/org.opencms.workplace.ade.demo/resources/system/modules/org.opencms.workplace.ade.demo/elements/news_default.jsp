<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.uri)" >
<div>
	<!-- Title Section of the news -->
	<h2><cms:contentshow element="Title" /></h2>
	<cms:contentcheck ifexists="SubTitle">
		<p><cms:contentshow element="SubTitle" /></p>
	</cms:contentcheck>
</div>	
</cms:contentload>
