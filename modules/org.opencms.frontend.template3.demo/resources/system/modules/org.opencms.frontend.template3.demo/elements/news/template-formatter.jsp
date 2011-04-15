<%@ page taglibs="c,cms,fn" import="org.opencms.file.*" %>
<div>
<cms:formatter var="content">

	<!-- Title Section of the news -->
	<h2>${content.value.Title}</h2>
	<c:if test="${content.value.SubTitle.isSet}">
				<p>${content.value.SubTitle}</p>
			</c:if>
	
	<!-- Optional image of the paragraph -->
	<c:forEach var="paragraph" items="${content.valueList.Paragraph}">
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
		
		<!-- Text of the paragraph -->
		${paragraph.value.Text}
	
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
	
	<!-- Author of the news -->
	<p>
		<c:choose>
			<c:when test="${content.value.AuthorMail.isSet}"><a href="mailto:${content.value.AuthorMail}">${content.value.Author}</a></c:when>
			<c:otherwise>${content.value.Author}</c:otherwise>
		</c:choose>
	</p>
</cms:formatter>
</div>