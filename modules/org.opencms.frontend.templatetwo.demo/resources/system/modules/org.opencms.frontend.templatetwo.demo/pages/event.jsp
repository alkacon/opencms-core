<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<cms:include property="template" element="head" />

<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/templatetwo/demo/messages">

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">
	<cms:contentaccess var="event" scope="page" />
	<div class="view-event">
	
		<!-- Title of the event -->
		<h2><cms:contentshow element="Title" /></h2>
		
		<!-- Event Dates -->
		<p>
			<label><fmt:message key="tt.event.start" /></label>
			<c:choose>
				<c:when test="${event.value['EventDates/ShowTime'] == 'true'}">
					<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" /> Uhr
				</c:when>
				<c:otherwise>
					<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventDate'])}" dateStyle="LONG" type="date" />
				</c:otherwise>
			</c:choose>
	
			<c:if test="${event.value['EventDates/EventEndDate'].exists}">
				<br />
				<label><fmt:message key="tt.event.end" /></label>
				<c:choose>
					<c:when test="${event.value['EventDates/ShowTime'] == 'true'}">
						<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventEndDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" /> Uhr
					</c:when>
					<c:otherwise>
						<fmt:formatDate value="${cms:convertDate(event.value['EventDates/EventEndDate'])}" dateStyle="LONG" type="date" />
					</c:otherwise>
				</c:choose>
			</c:if>
	
			<c:if test="${!event.value['EventDates/DateRemark'].isEmpty}">
				<br />
				<label><fmt:message key="tt.event.remarks" /></label>
				<cms:contentshow element="EventDates/DateRemark" />
			</c:if>
		</p>
	
		<hr class="separator" />
	
		<!-- Paragraph of the event -->
		<cms:contentloop element="Paragraph">
			<cms:contentloop element="Image">
				<c:set var="imagePath"><cms:contentshow element="Image" /></c:set>
				<c:set var="imageTitle">${cms:vfs(pageContext).property[imagePath]['Title']}</c:set>
				<c:set var="imageFolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imagePath")) %></c:set>
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

<cms:include property="template" element="foot" />