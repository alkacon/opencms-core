<%@page taglibs="c,cms" %>
<cms:formatter var="content" val="value">
	<cms:include file="${value.FunctionProvider}">
		<c:forEach var="parameter" items="${content.valueList.Parameter}">
			<cms:param name="${parameter.value.Key}" value="${parameter.value.Value}" />
		</c:forEach>
	</cms:include>
</cms:formatter>
