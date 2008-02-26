<%@ page import="org.opencms.jsp.*, org.opencms.util.*, java.util.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<cms:contentload collector="singleFile" param="${param.file}" editable="true">
	<cms:contentaccess var="content" />
	<div class="box ${param.schema}">
		<h4><c:out value="${content.value['Title']}" /></h4>
		<div class="boxbody">
			<c:out value="${content.value['Content']}" escapeXml="false" />

			<c:if test="${content.hasValue['JspFile']}">
				<c:set var="path" value="${content.value['JspFile'].stringValue}" />
				<% 
					String path = (String)pageContext.getAttribute("path");
					if (path.indexOf("?") < 0) {
						cms.include (path, null, false, null);
					} else {
						String[] section = CmsStringUtil.splitAsArray(path, "?"); 
						Map params = CmsStringUtil.splitAsMap(section[1], "&", "=");
						cms.include (section[0], null, false, params);
					}
				%>
			</c:if>	
		</div>
	</div>
</cms:contentload>