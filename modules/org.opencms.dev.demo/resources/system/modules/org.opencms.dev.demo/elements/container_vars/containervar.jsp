<%@page buffer="none" session="false" taglibs="c,cms" %>
<cms:formatter var="content" val="value">

	<div style="padding:8px;">					
			<h3>${value.Title}</h3>
			<p>${value.Content}</p>
			<span>&nbsp;</span>			
			<h4>Current container</h4>
			<p>Following container attributes are set in the template file:</p>
			<b>Name:</b> ${cms.container.name}<br/>
			<b>Type:</b> ${cms.container.type}<br/>
			<b>Width:</b> ${cms.container.width}<br/>
			<b>Max Elements:</b> ${cms.container.maxElements}</p>
			<span>&nbsp;</span>
			<h4>Container page</h4>
			<ul>
				<li><b>Locale:</b> ${cms.page.locale}</li>
				<li><b>Container Names:</b> 
					<ul><c:forEach var="con" items="${cms.page.names}" varStatus="status">
						<li>${con}</li>
					</c:forEach>
					</ul></li>
				<li><b>Container Types:</b> 
					<ul><c:forEach var="con" items="${cms.page.types}" varStatus="status">
						<li>${con}</li>
					</c:forEach>
					</ul></li>
			</ul>
			<span>&nbsp;</span>
			<h4>Element Mode:</h4>
			<p><b>Mode:</b> ${cms.edited}</p>
			<c:if test="${cms.edited}">
			<p>The element mode is <strong>true</strong>, if the element have been moved or edited, but the page have not been reloaded yet. 
			The element mode is <strong>false</strong>, if the element have not been changed since the last reload of the page.</p>
			</c:if>
	</div>

</cms:formatter>