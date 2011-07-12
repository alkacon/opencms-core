<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<div class="view-article">

	<%-- Title of the article --%>
	<h2>${value.Title}</h2>
	
	<%-- The text field of the article with image --%>
	<div class="paragraph">
		<c:set var="showing" value="false" />
		<c:if test="${value.Image.isSet}">
			<c:set var="showing" value="true" />						
			<c:set var="imgwidth">${((cms.container.width) / 2) - 25}</c:set>
			<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="left" alt="${value.Image}" title="${value.Image}" />						
		</c:if>						
		${value.Text}
		<c:if test="${showing}">
			<div class="clear"></div>					
		</c:if>
	</div>
</div>

</cms:formatter>