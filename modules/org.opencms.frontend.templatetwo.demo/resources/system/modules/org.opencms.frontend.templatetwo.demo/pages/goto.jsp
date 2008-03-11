<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:if test="${!empty param.uri && cms:vfs(pageContext).exists[param.uri]}">
	<c:redirect url="${cms:vfs(pageContext).link[param.uri]}" context="/" />
</c:if>

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