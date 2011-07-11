<%@page taglibs="c,cms" %>
<cms:formatter var="content" val="value">
	<c:choose>
		<c:when test="${value.FunctionProvider.isSet}">
			<cms:include file="${value.FunctionProvider}">
				<c:forEach var="parameter" items="${content.valueList.Parameter}">
					<cms:param name="${parameter.value.Key}" value="${parameter.value.Value}" />
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
