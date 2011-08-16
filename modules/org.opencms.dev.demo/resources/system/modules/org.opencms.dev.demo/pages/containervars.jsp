<%@page buffer="none" session="false" taglibs="c,cms" %>

<div style="padding:8px;">					
	<h3>Access of the container variables</h3>
	<c:if test="${cms.container.type == 'center'}"> 
	<p>
	Specific information about differnet containers on a container page as well as adjusted information of a container element is provided by
	org.opencms.jsp.util.CmsJspStandardContextBean. This bean is available from a jsp like formatter, as soon as the container page is rendered. 
	The values of the bean are adjusted depending on the element it is called from. 
	In order to access the CmsJspStandardContextBean from a jsp use a predefined EL variable \${cms}.</p>
	<p>		
	In this demo different variables available inside of the current element are displayed.
	These variables describe the current state of the container page and of the current container.<br>
	Please check the following jsp to learn more about the source code:<br/>
	<strong>/system/modules/org.opencms.dev.demo/pages/containervars.jsp</strong>
	</p>
	</c:if>
	<span>&nbsp;</span>			
	<h4>Current container</h4>
	<p>Following container attributes are defined in template:</p>
	<b>Name:</b> ${cms.container.name}<br/>
	<b>Type:</b> ${cms.container.type}<br/>
	<b>Width:</b> ${cms.container.width}<br/>
	<b>Max Elements:</b> ${cms.container.maxElements}</p>
	<span>&nbsp;</span>
	<h4>Container page</h4>
	<ul>
		<li><b>Locale:</b> ${cms.page.locale}</li>
		<li><b>Container Names:</b> 
			<ul>
				<c:forEach var="con" items="${cms.page.names}" varStatus="status">
					<li>${con}</li>
				</c:forEach>
			</ul>
		</li>
		<li><b>Container Types:</b> 
			<ul>
				<c:forEach var="con" items="${cms.page.types}" varStatus="status">
					<li>${con}</li>
				</c:forEach>
			</ul>
		</li>
	</ul>
	<span>&nbsp;</span>
	<h4>Element Mode:</h4>
	<p><b>Mode:</b> ${cms.edited}</p>
	<c:if test="${cms.edited}">
		<p>Please reload the current page.</p>
		<p>The element mode is <strong>true</strong>, if the element have been moved or edited, but the page have not been reloaded yet.<br/> 
		The element mode is <strong>false</strong>, if the element have not been changed since the last reload of the page.</p>
	</c:if>
	<c:if test="${!cms.edited}">
		<p>The element mode is <strong>true</strong>, if the element have been moved or edited, but the page have not been reloaded yet.<br/> 
		The element mode is <strong>false</strong>, if the element have not been changed since the last reload of the page.</p>
	</c:if>	
</div>