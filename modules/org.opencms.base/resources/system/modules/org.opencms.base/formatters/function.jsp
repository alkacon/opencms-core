<%@page session="false" %><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<cms:formatter var="content" val="value">
	<c:set var="format" value="${cms.functionFormatFromContent[content]}" />
	<c:choose>
		<c:when test="${format.exists}">
			<cms:include file="${format.jsp}">
				<c:forEach var="entry" items="${format.parameters}">
					<cms:param name="${entry.key}" value="${entry.value}" />
				</c:forEach>
			</cms:include>
		</c:when>
		<c:otherwise>
			<div style="border: 2px solid red; padding: 10px;">
			No JSP configured!
			</div>
		</c:otherwise>
	</c:choose>
</cms:formatter>
