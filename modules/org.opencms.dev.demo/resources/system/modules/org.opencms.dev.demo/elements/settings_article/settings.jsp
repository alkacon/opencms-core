<%@page buffer="none" session="false" taglibs="c,cms,fmt" %>
<fmt:setLocale value="${cms.locale}" />
<cms:formatter var="content" val="value">

<%-- The box schema changes the color of the box --%>
<c:set var="boxschema"><cms:elementsetting name="boxschema" default="box_schema1" /></c:set>
<div class="box ${boxschema}">


	<%-- Title of the article --%>	
	<h4><cms:elementsetting name="text" /></h4>
				
	<div class="boxbody">
		
		<%-- The text field of the article with image --%>		
		<div class="boxbody_listentry">
			
			<%-- Set the requied variables for the image. --%>
			<c:set var="showing" ><cms:elementsetting name="showimage" default="false" /></c:set>
			<c:if test="${showing && value.Image.isSet}">
				<c:set var="showing" value="true" />				
				<c:set var="imgclass">left</c:set> 
				<%-- The image is scaled to the one third of the container width, considering the padding=20px on both sides. --%>
				<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
				<cms:img src="${value.Image}" width="${imgwidth}" scaleColor="transparent" scaleType="0" cssclass="${imgclass}" alt="${value.Image}" title="${value.Image}" />				
			</c:if>
			
			<c:set var="date"><cms:elementsetting name="date" /></c:set>
			<c:set var="dateformat"><cms:elementsetting name="format" /></c:set>
			<c:if test="${not empty date}">
				<i><fmt:formatDate value="${cms:convertDate(date)}" dateStyle="SHORT" timeStyle="SHORT" type="${dateformat}" /></i>
				<br/>
			</c:if>			
			<c:if test="${value.Options.exists}">
				<c:forEach var="elem" items="${content.subValueList.Options}">					
					<c:choose>
						<c:when test="${elem.name == 'Text'}">
							${elem}
							<br/>
						</c:when>
						<c:when test="${elem.name == 'Html'}">
							${elem}
							<br/>
						</c:when>
						<c:when test="${elem.name == 'Link'}">
							<a href="<cms:link>${elem}</cms:link>">${elem}</a>
							<br/>
						</c:when>
					</c:choose>
				</c:forEach>
			</c:if>			
		</div>		
	</div>
	
	<h4>Setting values:</h4>
	<div class="boxbody">
		<p><b>Select box</b> "Box Color" = <cms:elementsetting name="boxschema" default="box_schema1" /></p>
		<p><b>Text field</b> "Text" = <cms:elementsetting name="text" /></p>
		<p><b>Check box</b> "Show Image" = <cms:elementsetting name="showimage" default="false" /></p>
		<p><b>Date picker</b> "Date" = <cms:elementsetting name="date" /></p>
		<p><b>Radio buttons</b> "Date Format" = <cms:elementsetting name="format" /></p>
	</div>

</div>

</cms:formatter>