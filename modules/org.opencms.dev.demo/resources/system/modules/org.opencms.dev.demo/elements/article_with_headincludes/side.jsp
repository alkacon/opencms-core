<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<%-- The used fancybox effect requires addition javascript and css, 
which are loaded in the html <head> of the template dynamiccaly using <cms:headinclude>.

The fancybox requires the page onLoad event to initialized.
Define to areas inside of the formatter:

<c:if test="${cms.edited}">
	Code to be displayed after the element was edited.				
</c:if> 

<c:if test="${!cms.edited}">
	Code to be displayed after the page was reloaded.
</c:if> 

--%>

<div class="box box_schema1">

	<%-- Title of the article --%>
	<h4>${value.Title}- Side</h4>
	<div class="boxbody">
		<%-- The text field of the article with image --%>		
		<div class="paragraph">
			<%-- Set the requied variables for the image. --%>
			<c:if test="${value.Image.isSet}">								
				<%-- The image is scaled to the one third of the container width, considering the padding=20px on both sides. --%>
				<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>				
				<c:if test="${cms.edited}">
					<%-- Do not surround the image by a link, if the element was just edited --%>
					<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" alt="${value.Image}" title="${value.Image}" />
				</c:if>
				<c:if test="${!cms.edited}">				
					<%-- surround the image by a link, if the page was reloaded, so the fancybox effect can be initialized --%>
					<a class="single_image" href="<cms:link>${value.Image}</cms:link>">
					<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="left" alt="${value.Image}" title="${value.Image}" />
					</a>
				</c:if>
			</c:if>	
			<c:if test="${cms.edited}">
				The element was just edited please reload the page, if you want to use the fancybox effect.
			</c:if>
			<c:if test="${!cms.edited}">				
				Please click on the image to see it using fancybox effect. Fancybox is only available, after the page was reloaded.
			</c:if>								
			${cms:trimToSize(cms:stripHtml(value.Text), 300)}
		</div>		
	</div>
</div>

</cms:formatter>