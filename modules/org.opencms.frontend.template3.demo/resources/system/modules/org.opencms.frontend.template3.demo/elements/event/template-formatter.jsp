<%@ page import="org.opencms.file.*" taglibs="c,cms,fn,fmt"%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/template3/demo/messages">
<cms:formatter var="content">
	<div class="view-event">
	
		<!-- Title of the event -->
		<h2>${content.value.Title}</h2>
		
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
	
		<!-- Optional image of the paragraph -->
	<c:forEach var="paragraph" items="${content.valueList.Paragraph}">
		<c:forEach var="image" items="${paragraph.valueList.Image}">
			<c:set var="imagePath">${image.value.Image}</c:set>
			<c:set var="imageName" value="${imagePath}" />
			<c:if test="${fn:indexOf(imagePath, '?') != - 1}">
				<c:set var="imageName" value="${fn:substringBefore(imagePath, '?')}" />
			</c:if>
			<c:set var="imageTitle">${content.vfs.property[imageName]['Title']}</c:set>
			<c:set var="imageFolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imageName")) %></c:set>
			<div class="image">
				<cms:img scaleType="1" width="200">
					<cms:param name="src">${imagePath}</cms:param> 
				</cms:img>
				<div class="description">
					${imageTitle}<br />
					<c:if test="${image.value.Description.isSet}">
						${image.value.Description}
					</c:if>
				</div>
			</div>
		</c:forEach>
		
		<!-- Optional headline of the paragraph -->
		<c:if test="${paragraph.value.Headline.isSet}"><h3>${paragraph.value.Headline}</h3></c:if>
		
		<!-- Text of the paragraph -->
		<p>${paragraph.value.Text}</p>
			<hr class="separator" />
		</c:forEach>
	</div>
</cms:formatter>
</fmt:bundle>