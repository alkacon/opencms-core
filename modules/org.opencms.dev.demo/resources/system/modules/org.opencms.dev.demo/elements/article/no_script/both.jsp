<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<div class="view-article">

	<%-- Main content of the article --%>
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
	<%-- End: Main content of the article --%>	
		
		<%-- Check if the script field is available. --%>
		<%-- ".isSet" checks if the Script node exists and not empty --%>
		<%-- ${cms.edited} marks that the element have been just edited --%>		
		<c:if test="${content.value.Script.value.Script.isSet && !cms.edited}">
			${content.value.Script.value.Script}
		</c:if>
		<c:if test="${content.value.Script.value.NoScript.isSet && cms.edited}">
			${content.value.Script.value.NoScript}
		</c:if>
		<c:if test="${!content.value.Script.value.NoScript.isSet && cms.edited}">
			<p>Script result not available after edit operation.</p>
		</c:if>
	</div>
	
</div>

</cms:formatter>