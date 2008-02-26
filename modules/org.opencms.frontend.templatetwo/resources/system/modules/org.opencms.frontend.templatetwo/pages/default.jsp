<%@page session="false" import="org.opencms.jsp.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);

%>
<cms:include property="template" element="head" />

<c:set var="locale" value="${cms.requestContext.locale}" />
<fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.frontend.templatetwo.frontend">

<h1><fmt:message key="default.title" /></h1>

<fmt:message key="default.text" />


</fmt:bundle>
<cms:include property="template" element="foot" />