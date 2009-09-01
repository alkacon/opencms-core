<%@ page import="org.opencms.file.*,org.opencms.main.*,org.opencms.jsp.*,org.opencms.jsp.util.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.workplace.ade.demo.messages">
<cms:contentload collector="singleFile" param="%(opencms.element)">
	<cms:contentaccess var="content" />
	<%
		CmsJspContentAccessBean content = (CmsJspContentAccessBean)pageContext.getAttribute("content");
		CmsFile file = content.getFile();
		String type = OpenCms.getResourceManager().getResourceType(file).getTypeName();
		type = org.opencms.workplace.CmsWorkplaceMessages.getResourceName(content.getLocale(), type);
		pageContext.setAttribute("type", type);
	%>
	<li class="cms-item">
		<div class=" ui-widget-content">
			<div class="cms-head ui-state-hover">
				<div class="cms-navtext">
					<a class="cms-left ui-icon ui-icon-triangle-1-e"></a>
					<c:out value="${content.value.Title}" />
				</div>
				<span class="cms-title"><c:out value="${content.value.Title}"/></span> 
				<span class="cms-file-icon"></span>
			</div>
			<div class="cms-additional">
			    <!-- TODO: WHY NOT TO USE THE TITLE ATTRIBUTE?? -->
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE_1"><fmt:param value="${content.file.rootPath}"/></fmt:message>">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE_0" /></span>${content.file.rootPath}
				</div>
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE_1"><fmt:param value="${type}"/></fmt:message>">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE_0" /></span>${type}
				</div>
			</div>
		</div>
	</li>

</cms:contentload>
</fmt:bundle>
