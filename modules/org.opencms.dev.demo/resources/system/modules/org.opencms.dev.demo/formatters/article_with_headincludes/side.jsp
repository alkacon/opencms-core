<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

<%-- This simple example displays the current date on clicking the refresh button. 

The required javascript is loaded in the html <head> of the template dynamiccaly using <cms:headinclude>.

--%>

<div class="box box_schema1">

	<%-- Title of the article --%>
	<h4>${value.Title}</h4>
	<div class="boxbody">
		
			<%-- Use the displayDate from the impoerted script to refresh the date. --%>
			<p id="dateText">Please click the button to refresh the date.</p>
			<button type="button" onclick="displayDate()">Refresh date</button>
			<br/>
			<br/>		

			<%-- Set the required variables for the image. --%>
			<c:if test="${value.Image.isSet}">							
				<%-- The image is scaled to the one third of the container width, considering the padding=20px on both sides. --%>
				<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
				<%-- Output of the image using cms:img tag --%>
				<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="left" alt="${value.Image}" title="${value.Image}" />				
			</c:if>								
			${cms:trimToSize(cms:stripHtml(value.Text), 300)}	
	</div>
</div>

</cms:formatter>