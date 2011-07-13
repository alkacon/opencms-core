<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<%-- This simple example displays the current date on clicking the refresh button. 

The required javascript is loaded in the html <head> of the template dynamiccaly using <cms:headinclude>.

--%>

<div class="view-article">

	<%-- Title of the article --%>
	<h2>${value.Title}</h2>
	
	<%-- The text field of the article with image --%>
	<div class="paragraph">
		
		<%-- Use the displayDate from the impoerted script to refresh the date. --%>
		<p id="dateText">Please click the button to refresh the date.</p>
		<button type="button" onclick="displayDate()">Refresh date</button>
		<br/>
		<br/>

		<%-- Set the required variables for the image. --%>
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