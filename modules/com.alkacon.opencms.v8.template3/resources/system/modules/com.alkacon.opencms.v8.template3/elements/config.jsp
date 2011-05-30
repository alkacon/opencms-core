<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">
<div>

<c:if test="${cms.container.name == 'headercontainer'}">
	<c:choose>
		<c:when test="${value.Header.isSet}">
			${value.Header.resolveMacros}
		</c:when>
		<c:otherwise>
			
		</c:otherwise>
	</c:choose>
</c:if>

<c:if test="${cms.container.name == 'footercontainer'}">
	<c:choose>
		<c:when test="${value.Footer.isSet}">
			${value.Footer.resolveMacros}
		</c:when>
		<c:otherwise>
			
		</c:otherwise>
	</c:choose>
</c:if>

</div>
</cms:formatter>