<%@ page import="org.opencms.jsp.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
	<cms:contentaccess var="content" />
	<div class="box box_schema3">
		<h4><c:out value="${content.value['Title']}" /></h4>
		<div class="boxbody">
			<c:out value="${content.value['Content']}" escapeXml="false" />

			<c:if test="${content.hasValue['JspFile']}">
				<c:set var="path" value="${content.value['JspFile'].stringValue}" />
				<c:choose>
					<c:when test="${!fn:contains(path, '?')}">
						<cms:include file="${path}">
							<cms:param name="box.uri" value="%(opencms.element)" />
						</cms:include>
					</c:when>
					<c:otherwise>
						<c:set var="uriParams" value="${fn:split(path, '?')[1]}" />
						<cms:include file="${fn:split(path, '?')[0]}">
							<cms:param name="box.uri" value="%(opencms.element)" />
							<c:forTokens items="${uriParams}" delims="&" var="uriParam">
								<cms:param name="${fn:split(uriParam, '=')[0]}" value="${fn:split(uriParam, '=')[1]}" />
							</c:forTokens>
						</cms:include>
					</c:otherwise>
				</c:choose>
			</c:if>	
		</div>
	</div>
</cms:contentload>