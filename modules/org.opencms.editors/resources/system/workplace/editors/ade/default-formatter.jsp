<%@ page import="org.opencms.workplace.editors.ade.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
CmsDefaultFormatterHelper cms = new CmsDefaultFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.ade.messages">
<cms:contentload collector="singleFile" param="%(opencms.element)">
	<cms:contentaccess var="content" />
	<li class="cms-item">
		<div class="ui-widget-content ui-corner-all">
			<div class="cms-head ui-state-hover ui-corner-all">
				<div class="cms-navtext">
					<a class="cms-left ui-icon ui-icon-triangle-1-e"></a>
					<c:if test="${cms.new}" >
						<c:out value="${cms.typeName}" />
					</c:if>
					<c:if test="${!cms.new}" >
						<c:if test="${!empty content.vfs.property[content.file]['NavText']}" >
							<c:out value="${content.vfs.property[content.file]['NavText']}" />
						</c:if>
						<c:if test="${empty content.vfs.property[content.file]['NavText']}" >
							<c:out value="${content.vfs.property[content.file]['Title']}" />
						</c:if>
					</c:if>
				</div>
				<span class="cms-title"><c:out value="${content.vfs.property[content.file]['Title']}" /></span>
				<span class="cms-file-icon" style="background-image: url(${cms.iconPath});"></span>
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
