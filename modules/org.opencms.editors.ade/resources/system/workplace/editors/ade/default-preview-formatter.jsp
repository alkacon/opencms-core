<%@ page import="org.opencms.jsp.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<cms:contentload collector="singleFile" param="%(opencms.element)" >
	<cms:contentaccess var="content" />
	<h3><cms:property name="Title" file="${content.filename}"  /></h3>
	<div style="overflow:auto; height: 330px;">
		<c:forEach items="${content.names}" var="elem">
		<c:if test="${!content.value[elem].isEmptyOrWhitespaceOnly}">
			<div>
				<h4 style="float:left; clear: left; font-size: 12px; margin: 0px 5px 0px 0px;"><c:out value="${content.value[elem].name}" />:</h4>
				<div>
				<c:choose>
					<c:when test="${content.value[elem].typeName == 'OpenCmsHtml'}">${content.value[elem]}</c:when>
					<c:when test="${content.value[elem].typeName == 'OpenCmsDateTime'}"><fmt:formatDate type="both" dateStyle="medium" timeStyle="medium" value="${cms:convertDate(content.value[elem])}" /></c:when>
					<c:when test="${content.value[elem].typeName == 'OpenCmsVfsFile'}"><a href="<cms:link>${content.value[elem]}</cms:link>" target="_blank">${content.value[elem]}</a></c:when>
					<c:otherwise><c:out value="${content.value[elem]}" /></c:otherwise>
				</c:choose>
				</div>
				<!-- ${content.value[elem].typeName} -->
     		</div>
     	</c:if>
     	</c:forEach>
	</div>
</cms:contentload>