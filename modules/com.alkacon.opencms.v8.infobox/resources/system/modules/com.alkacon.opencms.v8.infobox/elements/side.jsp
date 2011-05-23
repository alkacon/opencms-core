<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<div class="box ${cms.element.settings.boxschema}">

	<%-- Title of the article --%>
	<h4>${value.Title}</h4>
	
	<div class="boxbody">

		<div class="paragraph">
			${value.Text}			
		</div>
		
		<c:if test="${value.FurtherInfo.value.Link.exists 
			&& (value.FurtherInfo.value.Link.value.VariableLink.isSet 
				|| value.FurtherInfo.value.Link.value.LinkGallery.isSet 
				|| value.FurtherInfo.value.Link.value.DownloadGallery.isSet
				)
			}">

			<c:choose>
				<c:when test="${value.FurtherInfo.value.Link.value.VariableLink.isSet}">
					<c:set var="infolink">${value.FurtherInfo.value.Link.value.VariableLink}</c:set>
				</c:when>
				<c:when test="${value.FurtherInfo.value.Link.value.LinkGallery.isSet}">
					<c:set var="infolink">${value.FurtherInfo.value.Link.value.LinkGallery}</c:set>
				</c:when>
				<c:when test="${value.FurtherInfo.value.Link.value.DownloadGallery.isSet}">
					<c:set var="infolink">${value.FurtherInfo.value.Link.value.DownloadGallery}</c:set>
				</c:when>
			</c:choose>
			
			<c:set var="infotext">${infolink}</c:set>
			<c:if test="${value.FurtherInfo.value.Description.isSet}">
				<c:set var="infotext">${value.FurtherInfo.value.Description}</c:set>
			</c:if>
			
			<div class="boxbody_listentry">
				<a href="<cms:link>${infolink}</cms:link>">${infotext}</a><br/>
			</div>
		</c:if>
	</div>
</div>

</cms:formatter>