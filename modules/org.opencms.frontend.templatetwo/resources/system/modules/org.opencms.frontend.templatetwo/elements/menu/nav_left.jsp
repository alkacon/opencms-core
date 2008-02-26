<%@page session="false" import="org.opencms.frontend.templatetwo.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%

	// This element defines the left navigation element.
	// It uses the CmsTemplateMenu class to provide some auxiliary methods
	// in order to prevent the use of scriplet code in this jsp.
	// For details on the CmsTemplateMenu class, see the source code which can
	// be found at the following VFS location:
	// /system/modules/org.opencms.frontend.templatetwo/java_src/CmsTemplateMenu.java

	CmsTemplateMenu cms = new CmsTemplateMenu(pageContext, request, response);
	cms.setElements(cms.getNavigation().getNavigationTreeForFolder(cms.getRequestContext().getUri(), 1, 3));
	pageContext.setAttribute("cms", cms);
%>

<div id="nav_left">
	<ul>
		<c:set var="oldLevel" value="" />
		<c:forEach items="${cms.elements}" var="elem">
			<c:set var="currentLevel" value="${elem.navTreeLevel}" />
			
			<c:choose>
				<c:when test="${empty oldLevel}"></c:when>
				<c:when test="${currentLevel > oldLevel}"><ul></c:when>
				<c:when test="${currentLevel == oldLevel}"></li></c:when>
				<c:when test="${oldLevel > currentLevel}">
					<c:forEach begin="${currentLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
				</c:when>
			</c:choose>
			
			<li><a href="<cms:link>${elem.resourceName}</cms:link>" <c:if test="${cms.isCurrent[elem]}">class="current"</c:if>>${elem.navText}</a>
			
			<c:set var="oldLevel" value="${currentLevel}" />
		</c:forEach>
		
		<c:forEach begin="${cms.topLevel+1}" end="${oldLevel}"></li></ul></c:forEach>
	</ul>
</div>

