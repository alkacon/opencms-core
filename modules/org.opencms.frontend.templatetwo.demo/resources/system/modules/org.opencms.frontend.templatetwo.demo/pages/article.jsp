<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<cms:include property="template" element="head"/>

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">
<div class="view-article">

	<h2><cms:contentshow element="Title" /></h2>

	<cms:contentloop element="Paragraphs">
		<cms:contentloop element="Image">
		<c:set var="image"><cms:contentshow element="Image" /></c:set>
		<c:set var="imagetitle">${cms:vfs(pageContext).property[image]['Title']}</c:set>
		<c:set var="imagefolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("image")) %></c:set>
		<div class="image">
			<cms:img scaleType="1" width="200">
				<cms:param name="src">${image}</cms:param> 
			</cms:img>
			<div class="description">
				<a href="<cms:link>${imagefolder}index.html#${imagetitle}</cms:link>">${imagetitle}</a><br />
				<cms:contentshow element="Description" />
			</div>
		</div>
		</cms:contentloop>

		<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>	
		<cms:contentshow element="Text" />
	</cms:contentloop>
</div>

<cms:contentcheck ifexists="Source">
<%-- Output the source if there is any --%>
<div class="view-source">
	<cms:contentshow element="Source" />
</div>
</cms:contentcheck>

</cms:contentload>

<cms:include property="template" element="foot" />