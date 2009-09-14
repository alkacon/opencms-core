<%@page session="false" import="org.opencms.jsp.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<div>
<c:set var="locale" value="${cms.requestContext.locale}"/>
<fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.frontend.template3.frontend">

<cms:contentload collector="singleFile" param="%(opencms.uri)" >
<cms:contentaccess var="content" />

<jsp:useBean id="search" class="org.opencms.search.CmsSearch" scope="request">
	<jsp:setProperty name="search" property="*"/>
	<% 
		search.init(cms.getCmsObject());
		search.setIndex(cms.property("search.index"));
		search.setMatchesPerPage(5);
		String[] search_root=new String[]{cms.property("search.root", null, "/")};
		search.setSearchRoots(search_root);
	%>
</jsp:useBean>

<c:if test="${!content.value['Text'].isEmptyOrWhitespaceOnly}">
	<div class="search_text">
		<c:out value="${content.value['Text']}" escapeXml="false"/>
	</div>
</c:if>

<c:if test="${param.action != null}">
	<c:catch var="searchError">
		<c:set var="result" value="${search.searchResult}"/>
	</c:catch>
</c:if>

<c:if test="${empty searchError}">
	<form id="searchForm" name="searchForm" action="<cms:link>${cms.cmsObject.requestContext.uri}</cms:link>" method="post">
		    <input type="hidden" name="action" value="search" />
			<input type="hidden" name="searchPage" value="1" />
		    <input type="text" name="query" value="${search.query}" />
		    <input type="submit" name="submit" value="<fmt:message key="search.button" />" />
	</form>
</c:if>

<c:if test="${param.action != null && empty result && empty searchError}">
	<!-- No Results -->
	<c:if test="${!content.value['NoResult'].isEmptyOrWhitespaceOnly}">
		<div class="search_noresult">
			<c:out value="${content.value['NoResult']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.action != null && !empty searchError}">
	<!-- Error occurred -->
	<c:if test="${!content.value['Error'].isEmptyOrWhitespaceOnly}">
		<div class="search_error">
			<c:out value="${content.value['Error']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.action != null && !empty result}">
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
					<a href="<cms:link><c:out value='${search.previousUrl}'/></cms:link>"><fmt:message key="search.previous" /></a>
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
						<a href="<cms:link><c:out value="${navPage.value}" /></cms:link>"><c:out value="${navPage.key}" /></a>
					</c:otherwise>
				</c:choose>
				<c:if test="${!status.last}">| </c:if>
			</c:forEach>
			
			<c:choose>
				<c:when test="${!empty search.nextUrl}">
					<a href="<cms:link><c:out value='${search.nextUrl}'/></cms:link>"><fmt:message key="search.next" /></a>
				</c:when>
				<c:otherwise>
					<fmt:message key="search.next" />
				</c:otherwise>
			</c:choose>
		</div>
	</c:if>
	<!-- END: Pagination -->
</c:if>

</cms:contentload>
</fmt:bundle>
</div>