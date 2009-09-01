<%@ page import="org.opencms.workplace.editors.ade.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
CmsDefaultFormatterHelper cms = new CmsDefaultFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.workplace.ade.demo.messages">
<cms:contentload collector="singleFile" param="%(opencms.element)">
	<cms:contentaccess var="content" />
	<li class="cms-item">
		<div class=" ui-widget-content">
			<div class="cms-head ui-state-hover">
				<div class="cms-navtext">
					<a class="cms-left ui-icon ui-icon-triangle-1-e"></a>
					<c:if test="${cms.new}" >
						<c:out value="${cms.typeName}" />
					</c:if>
					<c:if test="${!cms.new}" >
						<c:out value="${content.value.Title}" />
					</c:if>
				</div>
				<c:if test="${!cms.new}" >
 					<c:set var="title" value="..."/>
					<c:if test="${!empty content.value.Author}" >
						<c:set var="title" value="${content.value.Author}"/>
					</c:if>
				</c:if>
				<c:if test="${cms.new}" >
					<c:set var="title" ><fmt:message key="MESSAGE_DEFAULT_FORMATTER_TEXT_0"/></c:set>
				</c:if>
				<span class="cms-title"><c:out value="${title}"/></span> 
				<span class="cms-info-icon"></span>
			</div>
			<div class="cms-additional">
			    <!-- TODO: WHY NOT TO USE THE TITLE ATTRIBUTE?? -->
			    <c:if test="${cms.new}" >
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_FOLDER_1"><fmt:param value="${cms.newFolder}"/></fmt:message>">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_FOLDER_0" /></span>${cms.newFolder}
				</div>
			    </c:if>
			    <c:if test="${!cms.new}" >
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE_1"><fmt:param value="${cms.path}"/></fmt:message>">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE_0" /></span>${cms.path}
				</div>
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE_1"><fmt:param value="${cms.typeName}"/></fmt:message>">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE_0" /></span>${cms.typeName}
				</div>
			    </c:if>
			</div>
		</div>
	</li>

</cms:contentload>
</fmt:bundle>
