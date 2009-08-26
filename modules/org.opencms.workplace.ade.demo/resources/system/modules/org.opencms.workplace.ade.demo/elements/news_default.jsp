<%@ page import="org.opencms.file.*"%>
<%@ page import="org.opencms.main.*"%>
<%@ page import="org.opencms.jsp.*"%>
<%@ page import="org.opencms.jsp.util.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.workplace.ade.demo.messages">
<cms:contentload collector="singleFile" param="%(opencms.element)">
	<cms:contentaccess var="content" />
	<%
	        CmsJspContentAccessBean content = (CmsJspContentAccessBean)pageContext.getAttribute("content");
	        CmsFile file = content.getFile();
	        String type = OpenCms.getResourceManager().getResourceType(file).getTypeName();
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
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE" />  ${content.file.rootPath}">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_FILE" /></span>${content.file.rootPath}
				</div>
				<div alt="<fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE"/>${type}">
					<span class="cms-left"><fmt:message key="MESSAGE_DEFAULT_FORMATTER_TYPE" /></span>${type}
				</div>
			</div>
		</div>
	</li>

</cms:contentload>
</fmt:bundle>
