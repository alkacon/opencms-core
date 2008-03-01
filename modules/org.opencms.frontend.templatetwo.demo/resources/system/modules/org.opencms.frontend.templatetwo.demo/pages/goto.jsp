<%@ page import="org.opencms.file.types.*, org.opencms.file.*, org.opencms.jsp.*, org.opencms.main.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<% 
	CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
	CmsObject cms = jsp.getCmsObject();

	String redirectUri = request.getParameter("uri");

	if (redirectUri != null && cms.existsResource(redirectUri)) {
		if (OpenCms.getResourceManager().getResourceType(cms.readResource(redirectUri)).getTypeId() != CmsResourceTypePlain.getStaticTypeId()) {
			response.sendRedirect (OpenCms.getLinkManager().substituteLink(cms, redirectUri, null, true));
		}
	}
%>
<cms:include property="template" element="head"/>

<fmt:setLocale value="${cms.requestContext.locale}" />
<fmt:bundle basename="org.opencms.frontend.templatetwo.demo.messages">

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">
<div class="view-article">

	<h2><cms:contentshow element="Title" /></h2>

	<cms:contentloop element="Paragraphs">
		<cms:contentcheck ifexists="Title"><h3><cms:contentshow element="Title" /></h3></cms:contentcheck>	
		<p><cms:contentshow element="Text" /></p>
	</cms:contentloop>

	<strong><fmt:message key="${param.module}" /></strong>
</div>
</cms:contentload>

</fmt:bundle>

<cms:include property="template" element="foot"/>