<%@page session="false" taglibs="c,cms,fmt" import="com.alkacon.opencms.v8.list.*"%><%

	// This element creates the output for a list.
	// A list is a generic container which to create a list of XML contents
	// of any kind.
	// This container is configured by an XML configuration file defining the
	// collector to get the contents from the VFS and the mapping of the content
	// fields to be displayed.
	//
	// To prevent to put scriplet code in the JSP istself, all the logic of 
	// this process is encapsulated in the classes CmsList, CmsListEntry and
	// CmsListContentMapping
	//
	// For details on this those classes, see the source code which can
	// be found at the following VFS locations:
	// /system/modules/com.alkacon.opencms.v8.list/java_src/CmsListConfiguration.java
	// /system/modules/com.alkacon.opencms.v8.list/java_src/CmsListEntry.java
	// /system/modules/com.alkacon.opencms.v8.list/java_src/CmsListContentMapping.java

	CmsListConfiguration cms = new CmsListConfiguration(pageContext, request, response);
	pageContext.setAttribute("list", cms);
%>

<div class="box ${cms.element.settings.boxschema}">

	<fmt:setLocale value="${cms.locale}" />
	<fmt:bundle basename="com.alkacon.opencms.v8.list.workplace">
	<script type="text/javascript">
		var currentPage = 1;
		var lastPage = 1;
		var itemsPerPage = 0;
		var pageIndex = 1;
		var itemCount = 0;
		var itemLocale = "${cms.locale}";
		var listCenterPath = "<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.list/elements/center_singlepage.jsp:6693efd7-854a-11e0-8012-c96c1b6c43a9)</cms:link>";
		var listConfig = "${cms.element.sitePath}";
		var pageUri = "${cms.requestContext.uri}";
		var imgPos = "";
		var imgWidth = ${(cms.container.width - 20) / 3};
		var fmtPaginationPrev = "<fmt:message key="v8.list.pagination.previous" />";
		var fmtPaginationNext = "<fmt:message key="v8.list.pagination.next" />";
	</script>

	<cms:formatter var="listbox">

		<%-- Set the items per page --%>
		<c:set var="itemsperpage" value="1000" />
		<c:if test="${listbox.value['ItemsPerPage'].isSet}">
			<c:set var="itemsperpage" value="${listbox.value['ItemsPerPage'].stringValue}" />	
		</c:if>
		<%-- Set the image position --%>
		<c:set var="imgpos"><cms:elementsetting name="imgalign" default="${listbox.value['PositionImage']}" /></c:set>
		<%-- Set the image width --%>
		<c:if test="${imgpos == 'top' || imgpos == 'left' || imgpos == 'right'}">
			<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
			<c:if test="${imgpos == 'top'}"><c:set var="imgwidth">${cms.container.width - 22}</c:set></c:if>
		</c:if>

		<%-- Title of the list box --%>
		<h4><c:out value="${listbox.value['Title']}" escapeXml="false" /></h4>

	</cms:formatter>
	
	<div class="boxbody">

		<%-- Text of the list box --%>
		<c:if test="${listbox.value['Text'].isSet}">
			<div class="boxbody_listentry">
				<c:out value="${listbox.value['Text']}" escapeXml="false" /><br/>
			</div>
		</c:if>
		
		<%-- Entries of the list box --%>
		<c:if test="${listbox.value['Parameter'].isSet && not cms.element.inMemoryOnly}">
			<cms:contentload collector="${listbox.value['Collector']}" param="${list.parameter}" preload="true" >
			
				<cms:contentinfo var="info" />			
				<c:if test="${info.resultSize > 0}">
					<cms:contentload editable="false" pageSize="%(pageContext.itemsperpage)" pageIndex="%(param.pageIndex)" pageNavLength="5">
						<cms:contentinfo var="innerInfo" scope="request" />
					</cms:contentload>

						<c:if test="${!cms.edited && innerInfo.resultSize > innerInfo.pageSize}">
							<script type="text/javascript">
								$(document).ready(function() {
									itemsPerPage = ${itemsperpage};
									itemCount = ${innerInfo.resultSize};
									imgPos = "${imgpos}";
									imgWidth = ${imgwidth};
		      							initPagination();
		      						});
							</script>
						</c:if>

				</c:if>
			</cms:contentload>
						
			<div id="list_center_pages">
				
				<c:choose>
				<c:when test="${!cms.requestContext.currentProject.onlineProject && (innerInfo.resultSize > innerInfo.pageSize)}">
					<c:set var="pages" value="${innerInfo.resultSize / itemsperpage}" />
					<c:if test="${(innerInfo.resultSize % itemsperpage) > 0}">
						<c:set var="pages" value="${pages + 1}" />
					</c:if>
					<c:forEach begin="1" end="${pages}" varStatus="status">
					<div id="list_center_page_${status.count}"<c:if test="${status.count > 1}"> style="display: none;"</c:if>>
						<%-- Show the links in the given path --%>
						<cms:include file="%(link.weak:/system/modules/com.alkacon.opencms.v8.list/elements/center_singlepage.jsp:6693efd7-854a-11e0-8012-c96c1b6c43a9)">
							<cms:param name="pageUri" value="${cms.requestContext.uri}" />
							<cms:param name="__locale" value="${cms.locale}" />
							<cms:param name="imgPos" value="${imgpos}" />
							<cms:param name="imgWidth" value="${imgwidth}" />
							<cms:param name="itemsPerPage" value="${itemsperpage}" />
							<cms:param name="collectorParam" value="${list.parameter}" />
							<cms:param name="pageIndex" value="${status.count}" />
							<cms:param name="listConfig" value="${cms.element.sitePath}" />
						</cms:include>
					</div>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<div id="list_center_page_1">
						<%-- Show the links in the given path --%>
						<cms:include file="%(link.weak:/system/modules/com.alkacon.opencms.v8.list/elements/center_singlepage.jsp:6693efd7-854a-11e0-8012-c96c1b6c43a9)">
							<cms:param name="pageUri" value="${cms.requestContext.uri}" />
							<cms:param name="__locale" value="${cms.locale}" />
							<cms:param name="imgPos" value="${imgpos}" />
							<cms:param name="imgWidth" value="${imgwidth}" />
							<cms:param name="itemsPerPage" value="${itemsperpage}" />
							<cms:param name="collectorParam" value="${list.parameter}" />
							<cms:param name="pageIndex" value="${param.pageIndex}" />
							<cms:param name="listConfig" value="${cms.element.sitePath}" />
						</cms:include>
					</div>
				</c:otherwise>
				</c:choose>
			</div>
			
			<c:if test="${innerInfo.resultSize > innerInfo.pageSize}">
				<c:choose>
					<c:when test="${cms.edited}" >
						<div class="boxbody_listentry"><fmt:message key="v8.list.pagination.reload" /></div>
					</c:when>
					<c:otherwise>
						<div class="boxbody_listentry"><div id="pagination" class="pagination"></div></div>
					</c:otherwise>
				</c:choose>
			</c:if>

			
		</c:if>

		<%-- Bottom Text of the list box --%>
		<c:if test="${listbox.value['TextBottom'].isSet}">
			<div class="boxbody_listentry">
				<c:out value="${listbox.value['TextBottom']}" escapeXml="false" /><br/>
			</div>
		</c:if>

		<%-- Additional Link --%>
		<c:if test="${listbox.hasValue['AdditionalLink']}">
			<a href="<cms:link><c:out value="${listbox.value['AdditionalLink'].value['Link']}" /></cms:link>"><c:out value="${listbox.value['AdditionalLink'].value['Text']}" /></a>
		</c:if>

	</div>
	</fmt:bundle>

</div>