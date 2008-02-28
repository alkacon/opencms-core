<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />

<cms:include property="template" element="head" />

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">
<cms:contentaccess var="event" scope="page" />
<div class="view-event">

	<h2><cms:contentshow element="Title" /></h2>
	<p>
		<label>Beginn:</label>
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
		<label>Ende:</label>
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
		<label>Hinweise:</label>
			<cms:contentshow element="EventDates/DateRemark" />
		</c:if>
	</p>

	<hr class="separator" />

	<cms:contentloop element="Paragraph">
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

		<cms:contentcheck ifexists="Title"><h3><cms:contentshow element="Title" /></h3></cms:contentcheck>	
		<p><cms:contentshow element="Text" /></p>
		<hr class="separator" />
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