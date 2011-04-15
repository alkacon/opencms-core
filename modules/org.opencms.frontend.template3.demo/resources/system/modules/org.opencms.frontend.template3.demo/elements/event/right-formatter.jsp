<%@ page import="org.opencms.file.*" taglibs="c,cms,fmt"%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/template3/demo/messages">
<cms:formatter var="content">
<div class="box box_schema2">
	
		<!-- Title of the event -->
		<h4>${content.value.Title}</h4>
<div class="boxbody">		
		<!-- Event Dates -->
		<p>
			<label><fmt:message key="t3.event.start" /></label>
			<c:choose>
				<c:when test="${content.value['EventDates/ShowTime'] == 'true'}">
					<fmt:formatDate value="${cms:convertDate(content.value['EventDates/EventDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" />
				</c:when>
				<c:otherwise>
					<fmt:formatDate value="${cms:convertDate(content.value['EventDates/EventDate'])}" dateStyle="LONG" type="date" />
				</c:otherwise>
			</c:choose>
	
			<c:if test="${content.value['EventDates/EventEndDate'].exists}">
				<br />
				<label><fmt:message key="t3.event.end" /></label>
				<c:choose>
					<c:when test="${content.value['EventDates/ShowTime'] == 'true'}">
						<fmt:formatDate value="${cms:convertDate(content.value['EventDates/EventEndDate'])}" dateStyle="LONG" timeStyle="SHORT" type="both" />
					</c:when>
					<c:otherwise>
						<fmt:formatDate value="${cms:convertDate(content.value['EventDates/EventEndDate'])}" dateStyle="LONG" type="date" />
					</c:otherwise>
				</c:choose>
			</c:if>
	
			<c:if test="${!content.value['EventDates/DateRemark'].isEmpty}">
				<br />
				<label><fmt:message key="t3.event.remarks" /></label>
				<cms:contentshow element="EventDates/DateRemark" />
			</c:if>
		</p>
	
		<hr class="separator" />
	
		<!-- Paragraph of the event -->
		<c:forEach var="paragraph" items="${content.valueList.Paragraph}">
			<!-- The optional headline of the paragraph -->
			<c:if test="${paragraph.value.Headline.isSet}"><h3>${paragraph.value.Headline}</h3></c:if>
		</c:forEach>
</div>
	</div>
</cms:formatter>
</fmt:bundle>