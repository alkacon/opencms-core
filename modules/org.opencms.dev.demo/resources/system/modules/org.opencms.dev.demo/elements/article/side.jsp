<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<div class="box box_schema1">

	<%-- Title of the article --%>
	<h4>${value.Title}</h4>
	<div class="boxbody">
		<%-- The text field of the article with image --%>		
		<div class="paragraph">
			<%-- Set the requied variables for the image. --%>
			<c:if test="${value.Image.isSet}">								
				<%-- The image is scaled to the one third of the container width, considering the padding=20px on both sides. --%>
				<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
				<%-- Output of the image using cms:img tag --%>
				<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="left" alt="${value.Image}" title="${value.Image}" />				
			</c:if>									
			${cms:trimToSize(cms:stripHtml(value.Text), 300)}
		</div>		
	</div>
</div>

</cms:formatter>