<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>

<c:set var="locale"><cms:property name="locale" file="search" default="en" /></c:set>
<fmt:setLocale value="${locale}" />

<div class="box ${param.schema}">

	<cms:contentload collector="singleFile" param="${param.file}" editable="true">

		<cms:contentaccess var="linkbox" />

		<%-- Title of the link box --%>
		<h4><c:out value="${linkbox.value['Title']}" escapeXml="false" /></h4>
		
		<div class="boxbody">

			<%-- Description of the link box --%>
			<c:if test="${!linkbox.value['Description'].isEmptyOrWhitespaceOnly}">
				<div class="boxbody_listentry">
					<c:out value="${linkbox.value['Description']}" escapeXml="false" />
				</div>
			</c:if>

				
			<%-- Entries of the link box --%>
			<c:forEach items="${linkbox.valueList['Links']}" var="entry">
				<div class="boxbody_listentry">
					<h5><a href="<cms:link>${entry.value['Link']}</cms:link>">${entry.value['Title']}</a></h5>
					<c:if test="${!entry.value['Description'].isEmptyOrWhitespaceOnly}">
						<c:out value="${entry.value['Description']}" escapeXml="false"/>
					</c:if>
				</div>
			</c:forEach>

		</div>
		
	</cms:contentload>				
</div>
