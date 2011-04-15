<%@page session="false" taglibs="c,cms"%>
<cms:formatter var="content">
	<div class="box box_schema3">
		<%-- Title of the link box --%>
		<h4>${content.value.Title}</h4>
		<div class="boxbody">
			<%-- Description of the link box --%>
			<c:if test="${content.value.Description.isSet}">
				<div class="boxbody_listentry">${content.value.Description}</div>
			</c:if>
			<%-- Entries of the link box --%>
			<c:forEach items="${content.valueList.Links}" var="entry">
				<div class="boxbody_listentry">
					<h5><a href="<cms:link>${entry.value.Link}</cms:link>">${entry.value.Title}</a></h5>
					<c:if test="${entry.value.Description.isSet}">${entry.value.Description}</c:if>
				</div>
			</c:forEach>
		</div>
	</div>
</cms:formatter>
