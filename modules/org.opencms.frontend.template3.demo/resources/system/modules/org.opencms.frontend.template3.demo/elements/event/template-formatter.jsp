<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/template3/demo/messages">
<cms:contentload collector="singleFile" param="%(opencms.element)" editable="auto">
	<cms:contentaccess var="event" scope="page" />
	<div class="view-event">
	
		<!-- Title of the event -->
		<h2><cms:contentshow element="Title" /></h2>
		
		<!-- Event Dates -->
		<p>
			<label><fmt:message key="t3.event.start" /></label>
			<c:choose>
				<c:when test="${event.value['EventDates/ShowTime'] == 'true'}">
					<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" />
				</c:when>
				<c:otherwise>
					<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventDate'])}" dateStyle="LONG" type="date" />
				</c:otherwise>
			</c:choose>
	
			<c:if test="${event.value['EventDates/EventEndDate'].exists}">
				<br />
				<label><fmt:message key="t3.event.end" /></label>
				<c:choose>
					<c:when test="${event.value['EventDates/ShowTime'] == 'true'}">
						<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventEndDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" />
					</c:when>
					<c:otherwise>
						<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventEndDate'])}" dateStyle="LONG" type="date" />
					</c:otherwise>
				</c:choose>
			</c:if>
	
			<c:if test="${!event.value['EventDates/DateRemark'].isEmpty}">
				<br />
				<label><fmt:message key="t3.event.remarks" /></label>
				<cms:contentshow element="EventDates/DateRemark" />
			</c:if>
		</p>
	
		<hr class="separator" />
	
		<!-- Paragraph of the event -->
		<cms:contentloop element="Paragraph">
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
	
			<!-- The optional headline of the paragraph -->
			<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>
			
			<!-- The text content of the paragraph -->	
			<p><cms:contentshow element="Text" /></p>
			<hr class="separator" />
		</cms:contentloop>
	</div>
</cms:contentload>
</fmt:bundle>