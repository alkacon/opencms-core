<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/templatetwo/demo/messages">
<cms:contentload collector="singleFile" param="%(opencms.element)" >
	<cms:contentaccess var="event" scope="page" />
	<div class="view-event">
		<!-- Title of the event -->
		<h2><cms:contentshow element="Title" /></h2>
		<!-- Event Dates -->
		<p>
			<label>Date</label>
			<fmt:formatDate value="${cms:convertDate(event.value['EventDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" />
		</p>
		<hr class="separator" />
		<!-- Paragraph of the event -->
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
			<!-- The optional headline of the paragraph -->
			<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>
			
			<!-- The text content of the paragraph -->	
			<cms:contentshow element="Text" />
			<hr class="separator" />
		<br />
	</div>
</cms:contentload>
</fmt:bundle>
