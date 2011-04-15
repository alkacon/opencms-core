<%@ page taglibs="c,cms"%>
<cms:formatter var="content">
<div class="box box_schema2">

	<!-- Title of the article -->
	<h4>${content.value.Title}</h4>

<div class="boxbody">
	<!-- Paragraphs of the article -->
	<c:forEach var="paragraph" items="${content.valueList.Paragraphs}">
		<!-- Optional headline of the paragraph -->
		<c:choose><c:when test="${paragraph.value.Headline.isSet}"><h3>${paragraph.value.Headline}</h3></c:when>
		<c:otherwise>${paragraph.value.Text}</c:otherwise>
		</c:choose>
	</c:forEach>
</div>
</div>
</cms:formatter>