<%@ page session="false" import="org.opencms.frontend.templateone.modules.*" %><%--
--%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %><%--
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %><%

CmsTemplateModules cms = new CmsTemplateModules(pageContext, request, response);

// get currently active locale to initialize message bundle
String locale = cms.getRequestContext().getLocale().toString();
pageContext.setAttribute("locale", locale);

String folder = request.getParameter("folder");
String folderTitle = cms.property("Title", folder, "");

%><cms:contentload collector="${param.collector}" param="${param.folder}link_${number}.html|linklist|${param.count}" preload="true"><%--

--%><cms:contentinfo var="contentInfo" scope="request" /><%--
--%><c:if test="${! contentInfo.emptyResult}"><%--

--%><fmt:setLocale value="${locale}" /><%--
--%><fmt:bundle basename="org/opencms/frontend/templateone/modules/workplace"><%--

--%><div class="sidelist">
<p class="sidelisthead"><% if (!"".equals(folderTitle)) { out.print(folderTitle); } else { %><fmt:message key="links.headline" /><% } %></p>
<cms:contentload editable="true">

<p class="sidelistitem"><c:set var="href"><cms:contentshow element="Url" /></c:set><%
String href = (String)pageContext.getAttribute("href");
if (!href.startsWith("http") && !href.startsWith("ftp:") && !href.startsWith("mailto:")) {
	href = cms.link(href);
}

%><a href="<%= href %>" target="<cms:contentshow element="Target" />" title="<cms:contentshow element="Title" />"><b><cms:contentshow element="Title" /></b></a>
<cms:contentcheck ifexists="Description"><br><cms:contentshow element="Description" /></cms:contentcheck>
</p>

</cms:contentload></div><%--
--%></fmt:bundle></c:if></cms:contentload>