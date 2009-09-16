<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
<div class="box box_schema2">

	<!-- Title of the article -->
	<h4><cms:contentshow element="Title" /></h4>

<div class="boxbody">
	<!-- Paragraphs of the article -->
	<cms:contentloop element="Paragraphs">	
		<!-- Optional headline of the paragraph -->
		<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>
		<!-- or the text -->
		<cms:contentcheck ifexistsnone="Headline"><cms:contentshow element="Text" /></cms:contentcheck>
	</cms:contentloop>
</div>
</div>
</cms:contentload>