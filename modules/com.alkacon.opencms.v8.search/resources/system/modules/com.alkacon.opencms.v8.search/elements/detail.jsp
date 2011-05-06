<%@page session="false" taglibs="c,cms,fmt,fn" import="org.opencms.file.*"%>
<div>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.search.frontend">

<cms:formatter var="content" val="value">

<c:set var="cmsobj" value="${cms.vfs.cmsObject}" />
<c:set var="index">${value.Config.value.Index.stringValue}</c:set>
<c:set var="root">${value.Config.value.Root.stringValue}</c:set>
<c:set var="matches">${value.Config.value.Matches.stringValue}</c:set>

<jsp:useBean id="search" class="org.opencms.search.CmsSearch" scope="request">
	<jsp:setProperty name="search" property="*"/>
	<%
		// initialize the search bean
		search.init((CmsObject)pageContext.getAttribute("cmsobj"));
		search.setIndex((String)pageContext.getAttribute("index"));
		int matches = 5;
		try {
			matches = Integer.parseInt((String)pageContext.getAttribute("matches"));
		} catch (Exception e) {
			// ignore, use default for number of matches
		}
		search.setMatchesPerPage(matches);
		String[] search_root = new String[]{(String)pageContext.getAttribute("root")};
		search.setSearchRoots(search_root);
	%>
</jsp:useBean>

<h2>${value.Title}</h2>

<c:if test="${value['Text'].isSet}">
	<div class="search_text">
		<c:out value="${value['Text']}" escapeXml="false"/>
	</div>
</c:if>

<c:if test="${param.searchaction != null}">
	<c:catch var="searchError">
		<c:set var="result" value="${search.searchResult}"/>
	</c:catch>
</c:if>

<c:if test="${empty searchError}">
	<form id="searchForm" name="searchForm" action="<cms:link>${cms.requestContext.uri}</cms:link>" method="post">
		    <input type="hidden" name="searchaction" value="search" />
			<input type="hidden" name="searchPage" value="1" />
		    <input type="text" name="query" value="${search.query}" />
		    <input type="submit" name="submit" value="<fmt:message key="search.button" />" />
	</form>
</c:if>

<c:if test="${param.searchaction != null && empty result && empty searchError}">
	<!-- No Results -->
	<c:if test="${value['NoResult'].isSet}">
		<div class="search_noresult">
			<c:out value="${value['NoResult']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.searchaction != null && !empty searchError}">
	<!-- Error occurred -->
	<c:if test="${value['Error'].isSet}">
		<div class="search_error">
			<c:out value="${value['Error']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.searchaction != null && !empty result}">
	<!-- START: Result List -->
	<c:forEach var="item" items="${result}">
		<div class="search_result">		
			<a href="<cms:link>${item.path}</cms:link>"><strong><c:out value="${item.title}"/> (<c:out value="${item.score}"/>%)</strong></a><br/>
			<c:out value="${item.excerpt}" escapeXml="false"/><br/>
			<small><fmt:formatDate value="${item.dateLastModified}" type="both"/></small>
		</div>
	</c:forEach>
	<!-- END: Result List -->
	
	<!-- START: Pagination -->
	<c:if test="${fn:length(search.pageLinks)>0}">
		<div class="pagination">
			<c:choose>
				<c:when test="${!empty search.previousUrl}">
					<a href="<cms:link><c:out value='${fn:replace(search.previousUrl, "action=", "searchaction=")}'/></cms:link>"><fmt:message key="search.previous" /></a>
				</c:when>
				<c:otherwise>
					<fmt:message key="search.previous" />
				</c:otherwise>
			</c:choose>
			
			<c:forEach var="navPage" items="${search.pageLinks}" varStatus="status">		
				<c:choose>
					<c:when test="${search.searchPage == navPage.key}">
						<b><c:out value="${navPage.key}" /></b>
					</c:when>
					<c:otherwise>
						<a href="<cms:link><c:out value='${fn:replace(navPage.value, "action=", "searchaction=")}' /></cms:link>"><c:out value="${navPage.key}" /></a>
					</c:otherwise>
				</c:choose>
				<c:if test="${!status.last}">| </c:if>
			</c:forEach>
			
			<c:choose>
				<c:when test="${!empty search.nextUrl}">
					<a href="<cms:link><c:out value='${fn:replace(search.nextUrl, "action=", "searchaction=")}'/></cms:link>"><fmt:message key="search.next" /></a>
				</c:when>
				<c:otherwise>
					<fmt:message key="search.next" />
				</c:otherwise>
			</c:choose>
		</div>
	</c:if>
	<!-- END: Pagination -->
</c:if>

</cms:formatter>
</fmt:bundle>
</div>