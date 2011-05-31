<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">
	<div class="container-box color-yellow">
		<%-- Title of the article --%>
		<h5>${value.Title}</h5>
		<p><i>This formatter has type="leftbox" attribute set in the resource xsd file.</i></p>
		<%-- Text of the article --%>
		<p>${value.Text}</p>
	</div>
</cms:formatter>