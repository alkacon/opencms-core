<%@page session="false" taglibs="c,cms,fmt,fn" %><%--

	to use this JSP as a function provider the parameter 'searchformlink'
	needs to be set and point to the search result page

 --%><fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.search.frontend">
<div class="box ${cms.element.settings.boxschema}">
	<c:choose>
		<c:when test="${empty param.searchformlink}">
			<h4><fmt:message key="search.configerror.title" /></h4>
			<div class="boxbody"><fmt:message key="search.configerror.message" /></div>
		</c:when>
		<c:otherwise>
			<c:set var="formlink"><cms:link>${param.searchformlink}</cms:link></c:set>
			<h4><fmt:message key="search.title" /></h4>
			<div class="boxbody">
			<form id="searchFormSide" name="searchForm" action="${formlink}" method="post">
				<input type="hidden" name="searchaction" value="search" />
				<input type="hidden" name="searchPage" value="1" />
				<input type="text" name="query" value="${search.query}" />
				<input type="submit" name="submit" value="<fmt:message key="search.button" />" />
			</form>
			</div>
		</c:otherwise>
	</c:choose>
</div>
</fmt:bundle>