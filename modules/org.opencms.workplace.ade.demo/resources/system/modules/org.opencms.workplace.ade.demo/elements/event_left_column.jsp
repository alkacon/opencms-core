<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/templatetwo/demo/messages">
<cms:contentload collector="singleFile" param="%(opencms.uri)" >
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
	</div>
</cms:contentload>
</fmt:bundle>
