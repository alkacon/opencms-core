<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">
	<div class="container-box color-rose">
		<%-- Title of the article --%>
		<h5>${value.Title}</h5>
		<p><i>This formatter has the maxwidth="500" attribute set in the resource xsd file.</i></p>
		<%-- Text of the article --%>
		<p>${value.Text}</p>
	</div>
</cms:formatter>