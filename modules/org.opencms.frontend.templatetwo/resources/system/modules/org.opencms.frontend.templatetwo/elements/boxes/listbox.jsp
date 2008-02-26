<%@page session="false" import="org.opencms.frontend.templatetwo.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%

	CmsListBox cms = new CmsListBox(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>

<c:set var="locale"><cms:property name="locale" file="search" default="en" /></c:set>
<fmt:setLocale value="${locale}" />

<div class="box ${param.schema}">

	<cms:contentload collector="singleFile" param="${param.file}" editable="true">

		<cms:contentaccess var="listbox" />

		<%-- Title of the list box --%>
		<h4><c:out value="${listbox.value['Title']}" escapeXml="false" /></h4>
		
	</cms:contentload>
	
	<div class="boxbody">
	
		<%-- Image of the list box --%>
		<c:if test="${!listbox.value['Image'].isEmptyOrWhitespaceOnly}">
			<cms:img src="${listbox.value['Image']}" alt="${listbox.value['Title']}" scaleType="1" width="100" scaleQuality="90"/>
		</c:if>

		<%-- Text of the list box --%>
		<c:if test="${!listbox.value['Text'].isEmptyOrWhitespaceOnly}">
			<div class="boxbody_listentry">
				<c:out value="${listbox.value['Text']}" escapeXml="false" /><br/>
			</div>
		</c:if>
		
		<%-- Entries of the list box --%>
		<cms:contentload collector="${listbox.value['Collector']}" param="${cms.parameter}" preload="true" >
		
			<cms:contentinfo var="info" />			
			<c:if test="${info.resultSize > 0}">
				<cms:contentload editable="true">
					<cms:contentaccess var="resource" />
					<c:set var="entry" value="${cms.mappedEntry[resource.rawContent]}" />
					
					<div class="boxbody_listentry">
						<h5><a href="${entry.link}">${entry.title}</a></h5>
		
						<%-- Author at top position --%>
						<c:if test="${!empty entry.author && listbox.value['PositionAuthor'] == 'Top'}">
							<p><small>${entry.author}</small></p>
						</c:if>
		
						<%-- Date at top position --%>
						<c:if test="${!empty entry.date && listbox.value['PositionDate'] == 'Top'}">
							<p><small><fmt:formatDate value="${entry.date}" type="date"/></small></p>
						</c:if>
						
						<%-- Image at top|left|right position --%>
						<c:if test="${!empty entry.image && (listbox.value['PositionImage'] == 'Top' || listbox.value['PositionImage'] == 'Left' || listbox.value['PositionImage'] == 'Right')}">
							<c:choose>
								<c:when test="${listbox.value['PositionImage'] == 'Left'}"><c:set var="float" value="left" /></c:when>
								<c:when test="${listbox.value['PositionImage'] == 'Right'}"><c:set var="float" value="right" /></c:when>
							</c:choose>
		
							<div <c:if test="${!empty float}">class="${float}"</c:if>>
								<cms:img src="${entry.image}" alt="${entry.title}" scaleType="1" width="100" scaleQuality="90"/>
							</div>
						</c:if>
		
						<%-- The text of the entry --%>
						<c:if test="${!empty entry.text}">
							<div>
								<c:out value="${entry.text}" escapeXml="false" />
			
								<%-- The more link of the entry --%>
								<c:if test="${!listbox.value['More'].isEmptyOrWhitespaceOnly}">
									<a href="${entry.link}"><c:out value="${listbox.value['More']}" escapeXml="false" /></a>
								</c:if>
							</div>
						</c:if>
						
						<%-- Image at bottom position --%>
						<c:if test="${!empty entry.image && listbox.value['PositionImage'] == 'Bottom'}">
							<cms:img src="${entry.image}" alt="${entry.title}" scaleType="1" width="100" scaleQuality="90"/>
						</c:if>
						
						<%-- Author at bottom position --%>
						<c:if test="${!empty entry.author && listbox.value['PositionAuthor'] == 'Bottom'}">
							<p><small>${entry.author}</small></p>
						</c:if>
						
						<%-- Date at bottom position--%>
						<c:if test="${!empty entry.date && listbox.value['PositionDate'] == 'Bottom'}">
							<p><small><fmt:formatDate value="${entry.date}" type="date"/></small></p>
						</c:if>		
					</div>
				</cms:contentload>
			</c:if>
			
			<%-- Additional Link --%>
			<c:if test="${listbox.hasValue['AdditionalLink']}">
				<a href="<cms:link><c:out value="${listbox.value['AdditionalLink'].value['Link']}" /></cms:link>"><c:out value="${listbox.value['AdditionalLink'].value['Text']}" /></a>
			</c:if>
		</cms:contentload>
		
	</div>
				
</div>
