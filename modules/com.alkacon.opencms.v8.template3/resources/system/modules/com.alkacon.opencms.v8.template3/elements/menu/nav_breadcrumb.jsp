<%@page buffer="none" session="false" taglibs="c,cms,fn" %>
<c:set var="navStartLevel" ><cms:property name="NavStartLevel" file="search" default="0" /></c:set>
<cms:navigation type="breadCrumb" startLevel="${navStartLevel+1}" endLevel="-1" var="nav" param="true" />

<c:set var="first" value="true" />
<c:forEach items="${nav.items}" var="elem">
	<c:set var="navText">${elem.navText}</c:set>
	<c:if test="${empty navText or fn:contains(navText, '??? NavText')}">
		<c:set var="navText">${elem.title}</c:set>
	</c:if>
	<c:if test="${!empty navText}">
		<c:if test="${!first}">&nbsp;»&nbsp;</c:if>
		<a href="<cms:link>${elem.resourceName}</cms:link>">${navText}</a>
		<c:set var="first" value="false" />
	</c:if>
</c:forEach>

        <c:set var="navTextOnFolder" value="${navText}" />
	<c:set var="navText"><cms:property name="NavText" /></c:set>
	<c:if test="${empty navText or fn:contains(navText, '??? NavText')}">
		<c:set var="navText"><cms:property name="Title" /></c:set>
	</c:if>
	<c:if test="${!empty navText and (navText ne navTextOnFolder)}">
		<c:if test="${!first}">
			&nbsp;»&nbsp;
		</c:if>
		<c:out value="${navText}" />
	</c:if>