<%@page session="false" import="org.opencms.jsp.*, org.opencms.file.*,java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// in order to omit folder levels, set the NavStartLevel property to the level number where 0 is the root "/" level
	int navStartLevel = Integer.parseInt(cms.property("NavStartLevel", "search", "0"));
    List navList =  cms.getNavigation().getNavigationForFolder(CmsResource.getPathPart(cms.getRequestContext().getFolderUri(), navStartLevel));
    pageContext.setAttribute("navList", navList);
	pageContext.setAttribute("cms", cms);
%>

<div id="nav_main" class="gradient">
<c:if test="${!empty navList}">
	<ul>
		<c:set var="oldLevel" value="" />
		<c:forEach items="${navList}" var="elem">
			<c:set var="currentLevel" value="${elem.navTreeLevel}" />
			
			<c:choose>
				<c:when test="${empty oldLevel}"></c:when>
				<c:when test="${currentLevel > oldLevel}"><ul></c:when>
				<c:when test="${currentLevel == oldLevel}"></li></c:when>
				<c:when test="${oldLevel > currentLevel}">
					<c:forEach begin="${currentLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
				</c:when>
			</c:choose>
			
			<li>
			<a href="<cms:link>${elem.resourceName}</cms:link>" <c:choose><c:when test="${fn:startsWith(cms.requestContext.uri, elem.resourceName)}">class="gradient current"</c:when><c:otherwise>class="gradient"</c:otherwise></c:choose>>${elem.navText}</a>
			
			<c:set var="oldLevel" value="${currentLevel}" />
		</c:forEach>
		
<%
        int topLevel = 0;
        if ((navList != null) && !navList.isEmpty()) {
            CmsJspNavElement elem = (CmsJspNavElement)navList.get(0);
            if (elem != null) {
                topLevel = elem.getNavTreeLevel();
            }
        }
        pageContext.setAttribute("topLevel", new Integer(topLevel));
%>		
		<c:forEach begin="${topLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
	</ul>
</c:if>
</div>
