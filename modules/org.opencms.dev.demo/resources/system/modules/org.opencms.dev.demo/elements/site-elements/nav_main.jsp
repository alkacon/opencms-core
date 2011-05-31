<%@page buffer="none" session="false" taglibs="c,cms,fn" %>
<c:set var="navStartLevel" ><cms:property name="NavStartLevel" file="search" default="0" /></c:set>
<cms:navigation type="forFolder" startLevel="${navStartLevel}" var="nav"/>
<div id="nav_main" class="gradient">
<c:if test="${!empty nav.items}">
	<ul>
		<c:set var="oldLevel" value="" />
		<c:forEach items="${nav.items}" var="elem">
			<c:set var="currentLevel" value="${elem.navTreeLevel}" />
			
			<c:choose>
				<c:when test="${empty oldLevel}"></c:when>
				<c:when test="${currentLevel > oldLevel}"><ul></c:when>
				<c:when test="${currentLevel == oldLevel}"></li></c:when>
				<c:when test="${oldLevel > currentLevel}">
					<c:forEach begin="${currentLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
				</c:when>
			</c:choose>
			
			<li>
			<a href="<cms:link>${elem.resourceName}</cms:link>" <c:choose><c:when test="${fn:startsWith(cms.requestContext.uri, elem.resourceName)}">class="gradient current"</c:when><c:otherwise>class="gradient"</c:otherwise></c:choose>>${elem.navText}</a>
			
			<c:set var="oldLevel" value="${currentLevel}" />
		</c:forEach>
		
		<c:forEach begin="${navStartLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
	</ul>
</c:if>
</div>