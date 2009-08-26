<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
<div class="box box_schema1">
	<!-- Title Section of the news -->
	<h4><cms:contentshow element="Title" /></h4>
	<cms:contentcheck ifexists="SubTitle">
		<p><cms:contentshow element="SubTitle" /></p>
	</cms:contentcheck>
	<!-- Author of the news -->
	<p>
		<cms:contentcheck ifexists="AuthorMail">
			<c:set var="authorMail"><cms:contentshow element="AuthorMail" /></c:set>
		</cms:contentcheck>
		<c:choose>
			<c:when test="${!empty authorMail}"><a href="mailto:${authorMail}"><cms:contentshow element="Author" /></a></c:when>
			<c:otherwise><cms:contentshow element="Author" /></c:otherwise>
		</c:choose>
	</p>
</div>
</cms:contentload>
