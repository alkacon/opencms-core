<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
<div class="view-article">

	<!-- Title of the article -->
	<h2><cms:contentshow element="Title" /></h2>

	<!-- Paragraphs of the article -->
	<cms:contentloop element="Paragraphs">
	
		<!-- Image of the paragraph -->
		<cms:contentloop element="Image">
			<c:set var="imagePath"><cms:contentshow element="Image" /></c:set>
			<c:set var="imageName" value="${imagePath}" />
			<c:if test="${fn:indexOf(imagePath, '?') != - 1}">
				<c:set var="imageName" value="${fn:substringBefore(imagePath, '?')}" />
			</c:if>
			<c:set var="imageTitle">${cms:vfs(pageContext).property[imageName]['Title']}</c:set>
			<c:set var="imageFolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imageName")) %></c:set>
			<div class="image">
				<cms:img scaleType="1" width="200">
					<cms:param name="src">${imagePath}</cms:param> 
				</cms:img>
				<div class="description">
					<a href="<cms:link>${imageFolder}index.html#${imageTitle}</cms:link>">${imageTitle}</a><br />
					<cms:contentcheck ifexists="Description">
						<cms:contentshow element="Description" />
					</cms:contentcheck>
				</div>
			</div>
		</cms:contentloop>

		<!-- Optional headline of the paragraph -->
		<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>
		
		<!-- The text content of the paragraph -->	
		<cms:contentshow element="Text" />
		
		<!-- The optional links for a paragraph -->
		<cms:contentcheck ifexists="Links">
			<ul>
			<cms:contentloop element="Links">
				<c:set var="newWindow"><cms:contentshow element="URI" /></c:set>
				<li><a href="<cms:link><cms:contentshow element="URI" /></cms:link>" <c:if test="${newWindow}">target="_blank"</c:if>>
					<cms:contentcheck ifexists="Description">
						<c:set var="desc"><cms:contentshow element="Description" /></c:set>
					</cms:contentcheck>
					<c:choose>
						<c:when test="${!empty desc}"><c:out value="${desc}" /></c:when>
						<c:otherwise><cms:contentshow element="URI" /></c:otherwise>
					</c:choose>
				</a></li>
			</cms:contentloop>
			</ul>
		</cms:contentcheck>
	</cms:contentloop>
</div>
</cms:contentload>