<%@page session="false" taglibs="c,cms,fmt,fn" import="org.opencms.file.*"%><%--

	to use this JSP as function provider the following parameters may be set:
	'searchIndex' to set the index searched, default: 'Online project EN (VFS)'
	'searchRoot' to set the folder to be searched, default: '/'
	'searchMatchesPerPage' to set the number of results displayed per page, default: '5'
	
 --%><div>
<fmt:setLocale value="${cms.locale}" />
<fmt:bundle basename="com.alkacon.opencms.v8.search.frontend">
<c:set var="cmsobj" value="${cms.vfs.cmsObject}" />
<%-- searching 'Online project EN (VFS)' by default, 
     supply function provider parameter 'searchIndex' to use any other --%>
<c:set var="index">${(empty param.searchIndex)? "Online project EN (VFS)" : param.searchIndex }</c:set>
<%-- searching from site root by default, 
     supply function provider parameter 'searchRoot' to use any other --%>
<c:set var="root">${(empty param.searchRoot)? "/" : param.searchRoot }</c:set>
<c:set var="matches">${(empty param.searchMatchesPerPage)? 5 : param.searchMatchesPerPage }</c:set>
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

<h2><fmt:message key="search.title" /></h2>
<c:set var="searchDecription"><fmt:message key="search.description" /></c:set>
<div class="search_text">
	<c:out value="${searchDescription}" escapeXml="false"/>
</div>

<c:if test="${param.searchaction != null}">
	<c:catch var="searchError">
		<c:set var="result" value="${search.searchResult}"/>
	</c:catch>
</c:if>

<c:if test="${empty searchError}">
	<form id="searchForm" name="searchForm" action="<cms:link>${cms.requestContext.uri}</cms:link>" method="post">
		    <input type="hidden" name="searchaction" value="search" />
			<input type="hidden" name="searchPage" value="1" />
		    <input type="text" name="query" value="<c:out value="${search.query}" />" />
		    <input type="submit" name="submit" value="<fmt:message key="search.button" />" />
	</form>
</c:if>

<c:if test="${param.searchaction != null && empty result && empty searchError}">
	<!-- No Results -->
	<div class="search_noresult">
		<fmt:message key="search.noresult" />
	</div>
</c:if>

<c:if test="${param.searchaction != null && !empty searchError}">
	<!-- Error occurred -->
	<div class="search_error">
		<fmt:message key="search.noresult" />
	</div>
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
</fmt:bundle>
</div>