<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%-- The import is only necessary to avoid highlighting snippets that destoy the HTML structure --%>
<%@ page import="org.opencms.util.CmsHtmlConverter"%>
<fmt:setLocale value="${cms.locale}" />
<cms:bundle basename="org.opencms.jsp.search.jsp-search-form-formatter">
<c:choose>
<c:when test="${cms.element.inMemoryOnly}">
	<h2><fmt:message key="content.inMemoryOnly"/></h2>
</c:when>
<c:when test="${cms.edited}">
	<div>
		<h2>
			<fmt:message key="content.edited" />
		</h2>
		<p>
			<fmt:message key="content.autoReload" />
			${cms.enableReload}
		</p>
	</div>
</c:when>
<c:otherwise>
<cms:formatter var="content">
	<%-- get the search form object containing results and controller --%>
	<cms:searchform var="searchform" configFile="${content.filename}" />
	<%-- short cut to access the controllers --%>
	<c:set var="controllers" value="${searchform.controller}" />
	<%-- short cut to access the controller for common search settings --%>
	<c:set var="common" value="${controllers.common}" />
	<div>
		<!-- The search form -->
		<!-- search action: link to the current page -->
		<form id="default-formatter-search-form" role="form" class="form-horizontal"
			action="<cms:link>${cms.requestContext.uri}</cms:link>">
			<!-- important: send this hidden field to have proper resetting of checked facet values and pagination -->
			<input type="hidden" name="${common.config.lastQueryParam}"
				value="${common.state.query}" />
			<%-- choose layout dependent on the presence of search options --%>
			<c:set var="hasSortOptions" value="${cms:getListSize(controllers.sorting.config.sortOptions) > 0}" />
			<c:set var="colWidthInput" value="${hasSortOptions?4:12}" />
			<div class="row">
				<div class="col-lg-${colWidthInput} col-md-${colWidthInput} col-sm-${colWidthInput} col-xs-12">
					<div class="input-group">
						<input name="${common.config.queryParam}" class="form-control"
							type="text" autocomplete="off" placeholder='<fmt:message key="form.enterquery" />'
							value="${fn:replace(common.state.query,'"','&quot;')}" /> <span class="input-group-btn">
							<button class="btn btn-primary" type="submit"><fmt:message key="button.submit" /></button>
						</span>
					</div>
				</div>
				<c:if test="${hasSortOptions}">
					<c:set var="sort" value="${controllers.sorting}" />
					<div class="col-lg-8 col-md-8 col-sm-8 col-xs-12">
						<div class="input-group">
							<!-- Display select box with sort options where the currently chosen option is selected -->
							<select name="${sort.config.sortParam}" class="form-control">
								<c:forEach var="option" items="${sort.config.sortOptions}">
									<option value="${option.paramValue}"
										${sort.state.checkSelected[option]?"selected":""}>${option.label}</option>
								</c:forEach>
							</select>
							<!-- Another button to send the form - just to improve handling -->
							<span class="input-group-btn">
								<button class="btn btn-primary" type="submit"><fmt:message key="button.sort"/></button>
							</span>
						</div>
					</div>
				</c:if>
			</div>
			<div class="row" style="margin-top: 20px;">
				<c:set var="hasFacets" value="${cms:getListSize(searchform.fieldFacets) > 0}" />
				<c:if test="${hasFacets}">
					<!-- Facets -->
					<div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
						<c:set var="fieldFacetControllers" value="${controllers.fieldFacets}" />
						<c:forEach var="facet" items="${searchform.fieldFacets}">
							<c:set var="facetController"
								value="${fieldFacetControllers.fieldFacetController[facet.name]}" />
							<c:if test="${cms:getListSize(facet.values) > 0}">
								<div class="panel panel-default">
									<div class="panel-heading">${facetController.config.label}</div>
									<div class="panel-body">
										<c:forEach var="facetItem" items="${facet.values}">
											<div class="checkbox">
												<label> <input type="checkbox"
													name="${facetController.config.paramKey}"
													value="${facetItem.name}"
													onclick="submitSearchForm()"
													${facetController.state.isChecked[facetItem.name]?"checked":""} />
													${facetItem.name} (${facetItem.count})
												</label>
											</div>
										</c:forEach>
									</div>
								</div>
							</c:if>
						</c:forEach>
					</div>
				</c:if>
			<!-- Search results -->
				<c:set var="colWidthResults" value="${hasFacets?8:12}" />
				<div class="col-lg-${colWidthResults} col-md-${colWidthResults} col-sm-${colWidthResults} col-xs-12">
					<c:choose>
						<c:when test="${empty searchform.searchResults}">
							<h3>
							<c:choose>
							<c:when test="${controllers.didYouMean.config.isEnabled && not empty searchform.didYouMean}" >
								<fmt:message key="results.didyoumean_1">
									<fmt:param><a href='<cms:link>${cms.requestContext.uri}?${searchform.didYouMeanLinkParameters}</cms:link>'>${searchform.didYouMean}</a></fmt:param>
								</fmt:message>
							</c:when>
							<c:otherwise>
								<fmt:message key="results.noResult" />														
							</c:otherwise>
							</c:choose>
							</h3>
						</c:when>
						<c:otherwise>
							<h3>
								<fmt:message key="result.heading"/>
								<small>
									<fmt:message key="result.info">
										<fmt:param value="${searchform.start}"/>
										<fmt:param value="${searchform.end}"/>
										<fmt:param value="${searchform.numFound}"/>
										<fmt:param value="${searchform.maxScore}"/>
									</fmt:message>
								</small>
							</h3>
							<hr />
							<!-- show search results -->
							<c:if test="${not empty searchform.highlighting}">
								<%! CmsHtmlConverter htmlConverter = new CmsHtmlConverter(); %>
							</c:if>
							<c:forEach var="searchResult" items="${searchform.searchResults}">
								<div>
									<a href='<cms:link>${searchResult.fields["path"]}</cms:link>'>${searchResult.fields["Title_prop"]}</a>
									<p>
										<!-- if highlighting is returned - show it; otherwise show content_en (up to 250 characters) -->
										<c:choose>
											<c:when test="${not empty searchform.highlighting}">
												<%-- To avoid destroying the HTML, if the highlighted snippet contains unbalanced tag, use the htmlConverter for cleaning the HTML. --%>
												<c:set var="highlightSnippet" value='${searchform.highlighting[searchResult.fields["id"]][searchform.controller.highlighting.config.hightlightField][0]}' />
												<%= htmlConverter.convertToString((String) pageContext.getAttribute("highlightSnippet")) %>
											</c:when>
											<c:otherwise>
												${cms:trimToSize(fn:escapeXml(searchResult.fields["content_en"]),250)}
											</c:otherwise>
										</c:choose>
									</p>
								</div>
								<hr />
							</c:forEach>
							<c:set var="pagination" value="${controllers.pagination}" />					
							<!-- show pagination if it should be given and if it's really necessary -->
							<c:if test="${not empty pagination && searchform.numPages > 1}">
								<ul class="pagination">
									<li ${pagination.state.currentPage > 1 ? "" : "class='disabled'"}>
										<a href="<cms:link>${cms.requestContext.uri}?${searchform.paginationLinkParameters['1']}</cms:link>"
										   aria-label='<fmt:message key="pagination.first.title"/>'>
											<span aria-hidden="true"><fmt:message key="pagination.first"/></span>
										</a>
									</li>
									<c:set var="previousPage">${pagination.state.currentPage > 1 ? pagination.state.currentPage - 1 : 1}</c:set>
									<li ${pagination.state.currentPage > 1 ? "" : "class='disabled'"}>
										<a href="<cms:link>${cms.requestContext.uri}?${searchform.paginationLinkParameters[previousPage]}</cms:link>"
										   aria-label='<fmt:message key="pagination.previous.title"/>'>
										   <span aria-hidden="true"><fmt:message key="pagination.previous"/></span>
										</a>
									</li>
									<c:forEach var="i" begin="${searchform.pageNavFirst}"
										end="${searchform.pageNavLast}">
										<c:set var="is">${i}</c:set>
										<li ${pagination.state.currentPage eq i ? "class='active'" : ""}><a
											href="<cms:link>${cms.requestContext.uri}?${searchform.paginationLinkParameters[is]}</cms:link>">${is}</a></li>
									</c:forEach>
									<c:set var="pages">${searchform.numPages}</c:set>
									<c:set var="next">${pagination.state.currentPage < searchform.numPages ? pagination.state.currentPage + 1 : pagination.state.currentPage}</c:set>
									<li	${pagination.state.currentPage >= searchform.numPages ? "class='disabled'" : ""}>
										<a aria-label='<fmt:message key="pagination.next.title"/>'
										   href="<cms:link>${cms.requestContext.uri}?${searchform.paginationLinkParameters[next]}</cms:link>">
											<span aria-hidden="true"><fmt:message key="pagination.next"/></span>
										</a>
									</li>
									<li	${pagination.state.currentPage >= searchform.numPages ? "class='disabled'" : ""}>
										<a aria-label='<fmt:message key="pagination.last.title"/>' href="<cms:link>${cms.requestContext.uri}?${searchform.paginationLinkParameters[pages]}</cms:link>">
											<span aria-hidden="true"><fmt:message key="pagination.last"/></span>
										</a>
									</li>
								</ul>
							</c:if>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</form>
		<script type="text/javascript">
			var searchForm = document.forms["default-formatter-search-form"];
			function submitSearchForm() {
				searchForm.submit();
			}
		</script>
	</div>
</cms:formatter>
</c:otherwise>
</c:choose>
</cms:bundle>