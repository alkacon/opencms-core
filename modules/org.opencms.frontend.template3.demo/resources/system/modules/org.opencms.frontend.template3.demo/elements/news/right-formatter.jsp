<%@ page taglibs="c,cms" %>
<div class="box box_schema2">
	<cms:formatter var="content">
	
		<!-- Title Section of the news -->
		<h4><a href="<cms:link>${content.filename}</cms:link>" style="color:inherit;">${content.value.Title}</a></h4>
		<div class="boxbody">
			<c:if test="${content.value.SubTitle.isSet}">
				<p>${content.value.SubTitle}</p>
			</c:if>
			<!-- Optional image of the paragraph -->
			<c:forEach var="paragraph" items="${content.valueList.Paragraph}">
			
				<!-- Optional headline of the paragraph -->
				<c:if test="${paragraph.value.Headline.isSet}"><h3>${paragraph.value.Headline}</h3></c:if>
				
			</c:forEach>
			<!-- Author of the news -->
			<p>
				<c:choose>
					<c:when test="${content.value.AuthorMail.isSet}"><a href="mailto:${content.value.AuthorMail}">${content.value.Author}</a></c:when>
					<c:otherwise>${content.value.Author}</c:otherwise>
				</c:choose>
			</p>
		</div>
	</cms:formatter>
</div>
