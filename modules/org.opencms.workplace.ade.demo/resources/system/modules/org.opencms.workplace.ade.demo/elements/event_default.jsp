<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org/opencms/frontend/templatetwo/demo/messages">
<cms:contentload collector="singleFile" param="%(opencms.element)" >
	<cms:contentaccess var="event" scope="page" />
	<div class="view-event">
		<!-- Title of the event -->
		<h2><cms:contentshow element="Title" /></h2>
	</div>
</cms:contentload>
</fmt:bundle>
