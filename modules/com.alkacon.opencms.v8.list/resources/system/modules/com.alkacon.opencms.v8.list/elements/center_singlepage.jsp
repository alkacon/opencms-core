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
	
	String configPath = request.getParameter("listConfig");
	CmsListConfiguration cms = new CmsListConfiguration(pageContext, request, response, configPath);
	pageContext.setAttribute("list", cms);
	String pageUri = request.getParameter("pageUri");
	String oldUri = cms.getRequestContext().getUri();
	cms.getRequestContext().setUri(pageUri);
%>

<fmt:setLocale value="${cms.locale}" />

	<cms:contentload collector="singleFile" param="%(param.listConfig)">
		<cms:contentaccess var="listbox" />
	</cms:contentload>
	<c:set var="imgpos" value="${param.imgPos}" />
	<c:set var="imgwidth" value="${param.imgWidth}" />
	<c:if test="${imgpos == 'top' || imgpos == 'left' || imgpos == 'right'}">
		<c:set var="cssFloat" value="" />
		<c:choose>
			<c:when test="${imgpos == 'left'}"><c:set var="cssFloat" value="left" /></c:when>
			<c:when test="${imgpos == 'right'}"><c:set var="cssFloat" value="right" /></c:when>
		</c:choose>
	</c:if>
		
	<%-- Entries of the list box --%>
	<c:if test="${!listbox.value['Collector'].isEmptyOrWhitespaceOnly}">
		<c:set var="listeditable" value="true" />
		<c:if test="${param.pageIndex > 1}">
			<c:set var="listeditable" value="false" />
		</c:if>
		<cms:contentload collector="${listbox.value['Collector']}" param="${list.parameter}" editable="${listeditable}" pageSize="%(param.itemsPerPage)" pageIndex="%(param.pageIndex)" pageNavLength="5" >
						
			<cms:contentaccess var="resource" />
			<c:set var="entry" value="${list.mappedEntry[resource.rawContent]}" />
			
			<div class="boxbody_listentry">
				<h5><a href="${entry.link}">${entry.title}</a></h5>

				<%-- Author at top position --%>
				<c:if test="${!empty entry.author && listbox.value['PositionAuthor'] == 'top'}">
					<p><small>${entry.author}</small></p>
				</c:if>

				<%-- Date at top position --%>
				<c:if test="${!empty entry.date && listbox.value['PositionDate'] == 'top'}">
					<p><small><fmt:formatDate value="${entry.date}" type="date"/></small></p>
				</c:if>
				
				<%-- Image at top|left|right position --%>
				<c:if test="${!empty entry.image}">
					<div<c:if test="${!empty cssFloat}"> class="${cssFloat}"</c:if>>
						<cms:img src="${entry.image}" alt="${entry.title}" scaleType="1" width="${imgwidth}" scaleQuality="90"/>
					</div>
				</c:if>

				<%-- The text of the entry --%>
				<c:if test="${!empty entry.text}">
					<div>
						<c:out value="${entry.text}" escapeXml="false" />
	
						<%-- The more link of the entry --%>
						<c:if test="${listbox.value['More'].isSet}">
							<a href="${entry.link}"><c:out value="${listbox.value['More']}" escapeXml="false" /></a>
						</c:if>
					</div>
				</c:if>
				
				<%-- Author at bottom position --%>
				<c:if test="${!empty entry.author && listbox.value['PositionAuthor'] == 'bottom'}">
					<p><small>${entry.author}</small></p>
				</c:if>
				
				<%-- Date at bottom position--%>
				<c:if test="${!empty entry.date && listbox.value['PositionDate'] == 'bottom'}">
					<p><small><fmt:formatDate value="${entry.date}" type="date"/></small></p>
				</c:if>		
			</div>

		</cms:contentload>
	</c:if>
<%
	cms.getRequestContext().setUri(oldUri);
%>