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

<fmt:setLocale value="${cms.locale}" />

<div class="box ${cms.element.settings.boxschema}">


	<cms:formatter var="listbox">

		<%-- Set the image position --%>
		<c:set var="imgpos"><cms:elementsetting name="imgalign" default="${listbox.value['PositionImage']}" /></c:set>
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
					<cms:contentload editable="true">
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
							<c:if test="${!empty entry.image && (imgpos == 'top' || imgpos == 'left' || imgpos == 'right')}">
								<c:set var="cssFloat" value="" />
								<c:set var="imgwidth">${(cms.container.width - 20) / 3}</c:set>
								<c:choose>
									<c:when test="${imgpos == 'left'}"><c:set var="cssFloat" value="left" /></c:when>
									<c:when test="${imgpos == 'right'}"><c:set var="cssFloat" value="right" /></c:when>
									<c:when test="${imgpos == 'top'}"><c:set var="imgwidth">${cms.container.width - 22}</c:set></c:when>
								</c:choose>
			
								<div <c:if test="${!empty cssFloat}">class="${cssFloat}"</c:if>>
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

			</cms:contentload>
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

</div>