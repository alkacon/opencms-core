<%@page session="false" taglibs="c,cms,fmt" %>

<fmt:setLocale scope="page" value="${param.locale}" />

<%-- Use the setting option to set the color schema of the box --%>
<c:set var="boxschema"><cms:elementsetting name="boxschema"/></c:set>
<div class="box ${boxschema}">
	<%-- Title of the article --%>
	<h4>Dynamic Function Demo.</h4>
	<div class="boxbody">
		<%-- The text field of the article with image --%>		
		<div class="paragraph">
				
			<jsp:useBean id="date" class="java.util.Date" />
			<%-- Use the setting option to define the format and style of the date time output. --%>
			<c:set var="format"><cms:elementsetting name="format"/></c:set>
			<c:set var="style"><cms:elementsetting name="style"/></c:set>
			
			<b><fmt:formatDate value="${date}" dateStyle="${style}" type="${format}" /></b>
			
			<h6>Settings:</h6>
			<ul>
				<li><b>Format:</b> <cms:elementsetting name="format"/></li>
				<li><b>Style:</b> <cms:elementsetting name="style"/></li>
				<li><b>Box Schema:</b> <cms:elementsetting name="boxschema"/></li>
			</ul>
			<h6>Parameters:</h6>
			<ul>
				<li><b>locale:</b> ${param.locale}</li>
			</ul>
		</div>		
	</div>
</div>