<%@page buffer="none" session="false" taglibs="c,cms,fmt" %>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com/alkacon/opencms/v8/news/messages">
<cms:formatter var="content" val="value">

<div class="box ${cms.element.settings.boxschema}">

	<%-- Title of the article --%>
	<h4>${value.Title}</h4>
	
	
	<div class="boxbody">
	
		<%-- Event Dates --%>			
		<p><i>
			<c:choose>		
				<c:when test="${cms.element.settings.showtime}">
					<fmt:formatDate value="${cms:convertDate(value.Date)}" dateStyle="SHORT" timeStyle="SHORT" type="both" />
				</c:when>
				<c:otherwise>
					<fmt:formatDate value="${cms:convertDate(value.Date)}" dateStyle="SHORT" type="date" />
				</c:otherwise>
			</c:choose>								
		</i></p>

		<%-- Paragraph of the news --%>
		<c:set var="paragraph" value="${value.Paragraph}" />
		<div class="paragraph">
			<c:set var="showimg" value="false" />
			<c:if test="${paragraph.value.Image.exists}">
				<c:set var="showimg" value="true" />
				<c:set var="imgalign"><cms:elementsetting name="imgalign" default="${paragraph.value.Image.value.Align}" /></c:set>
				<c:set var="imgclass"></c:set>
				<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
				<c:choose>
					<c:when test="${imgalign == 'top'}">
						<c:set var="imgwidth">${cms.container.width - 22}</c:set>
						<c:set var="imgclass">top</c:set>
					</c:when>
					<c:when test="${imgalign == 'left' || imgalign == 'lefthl'}">
						<c:set var="imgclass">left</c:set>
					</c:when>
					<c:when test="${imgalign == 'right' || imgalign == 'righthl'}">
						<c:set var="imgclass">right</c:set>
					</c:when>
				</c:choose>
			</c:if>
			<c:if test="${showimg && (imgalign == 'lefthl' || imgalign == 'righthl' || imgalign == 'top')}">
				<cms:img src="${paragraph.value.Image.value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="${imgclass}" alt="${paragraph.value.Image.value.Title}" title="${paragraph.value.Image.value.Title}" />
			</c:if>
			<%-- Optional headline of the paragraph --%>
			<c:if test="${paragraph.value.Headline.isSet}">
				<h5>${paragraph.value.Headline}</h5>
			</c:if>
			<c:if test="${showimg && (imgalign == 'left' || imgalign == 'right')}">
				<cms:img src="${paragraph.value.Image.value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="${imgclass}" alt="${paragraph.value.Image.value.Title}" title="${paragraph.value.Image.value.Title}" />
			</c:if>
			${cms:trimToSize(cms:stripHtml(paragraph.value.Text), 150)}
			<a href="<cms:link>${content.filename}</cms:link>"><fmt:message key="v8.news.readmore" /></a>
		</div>				
	</div>
</div>

</cms:formatter>
</fmt:bundle>