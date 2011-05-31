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

<div class="view-article">

	<%-- Title of the article --%>
	<h2>${value.Title} - Center </h2>
	
	<%-- The text field of the article with image --%>
	<div class="paragraph">
		<c:set var="showing" value="false" />
		<c:if test="${value.Image.isSet}">
			<c:set var="showing" value="true" />						
			<c:set var="imgwidth">${((cms.container.width) / 2) - 25}</c:set>
			<%-- Use ${cms.edited} variable to determine, if the element was just edited or not --%>
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
		${value.Text}
		<c:if test="${showing}">
			<div class="clear"></div>					
		</c:if>
	</div>
</div>

</cms:formatter>