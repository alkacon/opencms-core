<%@page session="false" import="org.opencms.frontend.templatetwo.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><% 

    // This element defines breadcrumb navigation.
    //
	// It uses the CmsTemplateMenu class to provide some auxiliary methods
	// in order to prevent the use of scriplet code in this jsp.
	// For details on the CmsTemplateMenu class, see the source code which can
	// be found at the followinf VFS location:
	// /system/modules/org.opencms.frontend.templatetwo/java_src/CmsTemplateMenu.java

	CmsTemplateMenu cms = new CmsTemplateMenu(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
	pageContext.setAttribute("navList", cms.getNavigation().getNavigationBreadCrumb(1, true));
%>

<c:set var="first" value="true" />
<c:forEach items="${navList}" var="elem" >
	<c:if test="${!empty cms.navText[elem]}">
		<c:if test="${!first}">&nbsp;&#187;&nbsp;</c:if>
		<a href="<cms:link>${elem.resourceName}</cms:link>">${cms.navText[elem]}</a>
		<c:set var="first" value="false" />
	</c:if>
</c:forEach>

<c:if test="${!cms.isDefault || first}">
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