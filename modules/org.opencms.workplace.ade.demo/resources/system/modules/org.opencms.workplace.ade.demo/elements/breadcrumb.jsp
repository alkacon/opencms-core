<%@page session="false" import="org.opencms.jsp.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><% 

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
	pageContext.setAttribute("navList", cms.getNavigation().getNavigationBreadCrumb(1, true));
%>

<c:set var="first" value="true" />
<c:forEach items="${navList}" var="elem" >
	<c:if test="${!empty elem.navText}">
		<c:if test="${!first}">&nbsp;&#187;&nbsp;</c:if>
		<a href="<cms:link>${elem.resourceName}</cms:link>">${elem.navText}</a>
		<c:set var="first" value="false" />
	</c:if>
	<c:if test="${empty elem.navText && !empty elem.title}">
		<c:if test="${!first}">&nbsp;&#187;&nbsp;</c:if>
		<a href="<cms:link>${elem.resourceName}</cms:link>">${elem.title}</a>
		<c:set var="first" value="false" />
	</c:if>
</c:forEach>
<%
        String path = CmsJspNavBuilder.getDefaultFile(
            cms.getCmsObject(),
            org.opencms.file.CmsResource.getFolderPath(cms.getRequestContext().getUri()));
        boolean isDefault = (path != null) &&
            path.equals(cms.getRequestContext().getUri());
        pageContext.setAttribute("cms", new Boolean(isDefault));
%>
<c:if test="${!isDefault || first}">
	<c:set var="navText"><cms:property name="NavText" /></c:set>
	<c:if test="${empty navText}">
		<c:set var="navText"><cms:property name="Title" /></c:set>
	</c:if>
	<c:if test="${!empty navText}">
		<c:if test="${!first}">
			&nbsp;&#187;&nbsp;
		</c:if>
		<c:out value="${navText}" />
	</c:if>
</c:if>