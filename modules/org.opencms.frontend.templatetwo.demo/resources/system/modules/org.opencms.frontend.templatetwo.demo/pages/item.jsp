<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:include property="template" element="head" />

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">
	<div class="view-item">
		<h2><cms:contentshow element="Name" /></h2>
	
		<cms:contentloop element="Images">
			<c:set var="imagePath"><cms:contentshow element="Image" /></c:set>
			<c:set var="imageParams" value="" />
			<c:if test="${fn:indexOf(imagePath, '?') != - 1}">
				<c:set var="imageParams" value="${fn:substringAfter(imagePath, '?')}" />
				<c:set var="imagePath" value="${fn:substringBefore(imagePath, '?')}" />
			</c:if>
			<c:set var="imageTitle">${cms:vfs(pageContext).property[imagePath]['Title']}</c:set>
			<c:set var="imageFolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imagePath")) %></c:set>
			<div class="image">
				<c:choose>
					<c:when test="${not empty imageParams}">
						<img src="<cms:link>${imagePath}?${imageParams}</cms:link>" alt="${imageTitle}">
					</c:when>
					<c:otherwise> 
						<cms:img scaleType="1" width="200" alt="${imageTitle}">
							<cms:param name="src">${imagePath}</cms:param> 
						</cms:img>
					</c:otherwise>
				</c:choose>
				<div class="description">
					<a href="<cms:link>${imageFolder}index.html#${imageTitle}</cms:link>">${imageTitle}</a><br />
					<cms:contentcheck ifexists="Description">
						<cms:contentshow element="Description" />
					</cms:contentcheck>
				</div>
			</div>
		</cms:contentloop>
	
		<div style="font-style:italic"><cms:contentshow element="ShortDescription" /></div><br/>
		<cms:contentshow element="LongDescription" />
		
		<!-- Optional links of the item -->
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
	</div>
	
</cms:contentload>

<cms:include property="template" element="foot" />