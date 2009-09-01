<%@page session="false" import="org.opencms.jsp.*,org.opencms.file.*,java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
    List navList =  cms.getNavigation().getNavigationTreeForFolder(cms.getRequestContext().getUri(), 1, 3);
    pageContext.setAttribute("navList", navList);
	pageContext.setAttribute("cms", cms);
%>
<%!
   private boolean isCurrent(CmsObject cms, CmsJspNavElement elem) {
                    String uri = cms.getRequestContext().getUri();
                    CmsJspNavElement uriElem = null;
                    try {
                        uriElem = new CmsJspNavElement(uri, CmsProperty.toMap(cms.readPropertyObjects(
                            uri,
                            false)));
                    } catch (Exception ex) {
                        // noop
                    }

                    // check if uri matches resource name
                    if (elem.getResourceName().equals(uri)) {
                        return true;
                    }

                    // check if the default file for the uri matches the resource name
                    String path = CmsJspNavBuilder.getDefaultFile(cms, elem.getResourceName());
                    if ((path == null) || ((uriElem != null) && uriElem.isInNavigation())) {
                        path = elem.getResourceName();
                    }

                    if (uri.equals(path)) {
                        return true;
                    }

                    // check if uri is in NOT in the navigation and so a parent folder will be marked as current
                    CmsJspNavElement navElem = uriElem;
                    while ((navElem != null) && !navElem.isInNavigation()) {

                        String parentPath = CmsResource.getParentFolder(navElem.getResourceName());
                        if (parentPath == null) {
                            break;
                        }
                        try {
                            navElem = new CmsJspNavElement(
                                parentPath,
                                CmsProperty.toMap(cms.readPropertyObjects(parentPath, false)));
                        } catch (Exception ex) {
                            break;
                        }
                    }

                    if ((navElem != null) && (uriElem != null) && !uriElem.isInNavigation()) {
                        return elem.equals(navElem);
                    }

                    return false;
}
%>

<div id="nav_left">
	<ul>
		<c:set var="oldLevel" value="" />
		<c:forEach items="${navList}" var="elem">
			<c:set var="currentLevel" value="${elem.navTreeLevel}" />
			
			<c:choose>
				<c:when test="${empty oldLevel}"></c:when>
				<c:when test="${currentLevel > oldLevel}"><ul></c:when>
				<c:when test="${currentLevel == oldLevel}"></li></c:when>
				<c:when test="${oldLevel > currentLevel}">
					<c:forEach begin="${currentLevel+1}" end="${oldLevel}"></li></ul></c:forEach></li>
				</c:when>
			</c:choose>
			
			<li><a href="<cms:link>${elem.resourceName}</cms:link>" <% if (isCurrent(cms.getCmsObject(), (CmsJspNavElement)pageContext.getAttribute("elem"))) {%>class="current"<%}%>${elem.navText}</a>
			
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
		<c:if test="${not empty navList}"></li></c:if>
	</ul>
</div>

