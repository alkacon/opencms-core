<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/template3/demo/messages">
<cms:contentload collector="singleFile" param="%(opencms.element)">
	<cms:contentaccess var="event" scope="page" />
<div class="box box_schema2">
	
		<!-- Title of the event -->
		<h4><cms:contentshow element="Title" /></h4>
<div class="boxbody">		
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
			<!-- The optional headline of the paragraph -->
			<cms:contentcheck ifexists="Headline"><h3><cms:contentshow element="Headline" /></h3></cms:contentcheck>
		</cms:contentloop>
</div>
	</div>
</cms:contentload>
</fmt:bundle>