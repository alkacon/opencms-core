<%@ page taglibs="c,cms" %>
<cms:formatter var="content">
<div class="box box_schema1">
		<h4>${content.value.Name}</h4>
	<div class="boxbody">	
		<div style="font-style:italic">${content.value.ShortDescription}</div><br/>
		
		<!-- Optional links of the paragraph -->
		<c:if test="${!empty paragraph.valueList.Links}">
			<ul>
				<c:forEach var="link" items="${paragraph.valueList.Links}">
					<li><a href="<cms:link>${link.value.URI}</cms:link>">
						<c:choose>
							<c:when test="${link.value.Description.isSet}">${link.value.Description}</c:when>
							<c:otherwise>${link.value.URI}</c:otherwise>
						</c:choose>
					</a></li>
				</c:forEach>
			</ul>
		</c:if>
	</div>
	</div>
</cms:formatter>
