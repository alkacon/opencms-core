<%@ page taglibs="c,cms,fn" import="org.opencms.file.*" %>
<cms:formatter var="content">
<div class="view-article">

	<!-- Title of the article -->
	<h2>${content.value.Title}</h2>

	<!-- Paragraphs of the article -->
	<c:forEach var="paragraph" items="${content.valueList.Paragraphs}">
	
		<!-- Image of the paragraph -->
		<c:forEach var="image" items="${paragraph.valueList.Image}">
			<c:set var="imagePath">${image.value.Image}</c:set>
			<c:set var="imageName" value="${imagePath}" />
			<c:if test="${fn:indexOf(imagePath, '?') != - 1}">
				<c:set var="imageName" value="${fn:substringBefore(imagePath, '?')}" />
			</c:if>
			<c:set var="imageTitle">${content.vfs.property[imageName]['Title']}</c:set>
			<c:set var="imageFolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imageName")) %></c:set>
			<div class="image">
				<cms:img scaleType="1" width="200">
					<cms:param name="src">${imagePath}</cms:param> 
				</cms:img>
				<div class="description">
					${imageTitle}<br />
					<c:if test="${image.value.Description.isSet}">
						${image.value.Description}
					</c:if>
				</div>
			</div>
		</c:forEach>

		<!-- Optional headline of the paragraph -->
		<c:if test="${paragraph.value.Headline.isSet}"><h3>${paragraph.value.Headline}</h3></c:if>
		
		<!-- The text content of the paragraph -->	
		${paragraph.value.Text}
		
		<!-- The optional links for a paragraph -->
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
	</c:forEach>
</div>
</cms:formatter>