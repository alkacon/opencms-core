<%@page session="false" import="org.opencms.jsp.*,org.opencms.file.*,org.opencms.relations.*,java.util.*"%>
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

<cms:contentload collector="singleFile" param="%(opencms.element)" >
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

<c:if test="${param.searchaction != null}">
	<c:catch var="searchError">
		<c:set var="result" value="${search.searchResult}"/>
	</c:catch>
</c:if>

<c:if test="${empty searchError}">
	<form id="searchForm" name="searchForm" action="<cms:link>${cms.cmsObject.requestContext.uri}</cms:link>" method="post">
		    <input type="hidden" name="searchaction" value="search" />
			<input type="hidden" name="searchPage" value="1" />
		    <input type="text" name="query" value="${search.query}" />
		    <input type="submit" name="submit" value="<fmt:message key="search.button" />" />
	</form>
</c:if>

<c:if test="${param.searchaction != null && empty result && empty searchError}">
	<!-- No Results -->
	<c:if test="${!content.value['NoResult'].isEmptyOrWhitespaceOnly}">
		<div class="search_noresult">
			<c:out value="${content.value['NoResult']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.searchaction != null && !empty searchError}">
	<!-- Error occurred -->
	<c:if test="${!content.value['Error'].isEmptyOrWhitespaceOnly}">
		<div class="search_error">
			<c:out value="${content.value['Error']}" escapeXml="false"/>
		</div>
	</c:if>
</c:if>

<c:if test="${param.searchaction != null && !empty result}">
	<!-- START: Result List -->
	<c:forEach var="item" items="${result}">
		<div class="search_result">
<%
String itemPath = cms.getCmsObject().getRequestContext().removeSiteRoot(((org.opencms.search.CmsSearchResult)pageContext.getAttribute("item")).getPath());
try {
  CmsResource res = cms.getCmsObject().readResource(itemPath);
  if (res.getTypeId() == 147) { //news
    itemPath = "/demo_t3/today/news/news.html?id=" +res.getStructureId();
  } else if (res.getTypeId() == 149) { //event
    itemPath = "/demo_t3/today/events/event.html?id=" +res.getStructureId();
  } else if (res.getTypeId() == 148) { //item
    String category = CmsCategoryService.getInstance().readResourceCategories(cms.getCmsObject(), itemPath).get(0).getName().toLowerCase();
    itemPath = "/demo_t3/dictionary/item_composite.html";
    if (category.contains("liliaceous")) {
      itemPath = "/demo_t3/dictionary/item_liliaceous.html";
    } else if (category.contains("rosaceous")) {
      itemPath = "/demo_t3/dictionary/item_rosaceous.html";
    } 
    itemPath += "?id=" + res.getStructureId();
  } else {
    List<CmsRelation> relations = cms.getCmsObject().getRelationsForResource(itemPath, CmsRelationFilter.SOURCES.filterType(CmsRelationType.XML_STRONG));
    if (relations.size()>1) {
      itemPath = cms.getCmsObject().getSitePath(relations.get(0).getSource(cms.getCmsObject(), CmsResourceFilter.ALL));
    }
  }
  pageContext.setAttribute("itemPath", itemPath);
} catch(Exception e) {
  out.println(org.opencms.main.CmsException.getStackTraceAsString(e));
  pageContext.setAttribute("itemPath", itemPath);
}
%>		
			<a href="<cms:link>${itemPath}</cms:link>"><strong><c:out value="${item.title}"/> (<c:out value="${item.score}"/>%)</strong></a><br/>
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

</cms:contentload>
</fmt:bundle>
</div>