<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
<div class="box box_schema3">
	<!-- Title Section of the info -->
	<h4><cms:contentshow element="Title" /></h4>
	<cms:contentshow element="Teaser" />
	
	<!-- Author of the info -->
	<cite>
		<cms:contentshow element="Author" />
	</cite>
</div>
</cms:contentload>
