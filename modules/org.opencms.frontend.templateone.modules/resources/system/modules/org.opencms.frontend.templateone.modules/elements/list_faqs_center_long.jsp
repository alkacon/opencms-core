<%@ page session="false" buffer="none" import="org.opencms.util.*, org.opencms.frontend.templateone.modules.*" %><%--
--%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %><%--
--%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %><%

CmsTemplateModules cms = new CmsTemplateModules(pageContext, request, response);

// get currently active locale to initialize message bundle
String locale = cms.getRequestContext().getLocale().toString();
pageContext.setAttribute("locale", locale);

boolean showNumber = Integer.parseInt(request.getParameter("elementcount")) == Integer.MAX_VALUE;
pageContext.setAttribute("shownumber", "" + showNumber);

String folder = cms.getCategoryFolder();
pageContext.setAttribute("catfolder", folder);

int count = cms.getResourceCount(folder, "faq");
pageContext.setAttribute("rescount", "" + count);

%><fmt:setLocale value="${locale}" /><%--
--%><fmt:bundle basename="org/opencms/frontend/templateone/modules/workplace"><%

// get first the navigation HTML which sets additionally some flags in the bean
String categoryNav = cms.buildHtmlNavList("faq", "style=\"margin-top: 4px;\"");

if (cms.showNavBreadCrumb()) {
%>
<hr noshade="noshade" size="1">
<b><fmt:message key="navbar.center.path" />:</b> <%= cms.buildHtmlNavBreadcrumb(" | ") %>
<hr noshade="noshade" size="1">
<%
}

if (cms.hasCategoryFolders()) {
%>
<p style="margin: 0px;">
<b><fmt:message key="navbar.center.categories" />:</b></p>
<%= categoryNav %>
<hr noshade="noshade" size="1">
<%
}
%>
<c:choose>
<c:when test="${(rescount != '0') && (rescount != '-1')}">

<cms:contentload collector="${param.collector}" param="${pageContext.catfolder}faq_${number}.html|faq|${param.elementcount}" editable="true" pageSize="${param.count}" pageIndex="${param.pageIndex}" pageNavLength="10"><%--
--%><cms:contentinfo var="contentInfo" scope="request" /><%--

--%><c:if test="${(contentInfo.resultIndex % contentInfo.pageSize) == 1}"><%--

	result size: <cms:contentinfo value="${contentInfo.resultSize}" />
	page size: <cms:contentinfo value="${contentInfo.pageSize}" />
	page count: <cms:contentinfo value="${contentInfo.pageCount}" />
	page index: <cms:contentinfo value="${contentInfo.pageIndex}" />

--%><c:if test="${shownumber == 'true' && contentInfo.pageCount > 1}"><p><fmt:message key="navbar.center.resultsize" />: <cms:contentinfo value="${contentInfo.resultSize}" /></c:if><%--
--%><c:if test="${contentInfo.resultSize > contentInfo.pageSize}">
&nbsp;|&nbsp;
<fmt:message key="navbar.center.pagelinks" />:&nbsp;
<c:forEach var="i" begin="${contentInfo.pageNavStartIndex}" end="${contentInfo.pageNavEndIndex}">
<c:choose>
<c:when test="${(i == param.pageIndex) || (i == 1 && param.pageIndex == null)}">
[<c:out value="${i}" />]&nbsp;
</c:when>
<c:otherwise>
[<a href="<cms:link><%= cms.getRequestContext().getUri() %>?pageIndex=<c:out value="${i}" />&categoryfolder=<c:out value="${catfolder}" /></cms:link>"><c:out value="${i}" /></a>]&nbsp;
</c:otherwise>
</c:choose>
</c:forEach>
</c:if>
<c:if test="${shownumber == 'true'}"></p></c:if>
</c:if>
<p style="margin-top: 8px; padding-top: 0px;">
<a href="<cms:link><cms:contentshow element="${opencms.filename}" />?uri=<%= cms.getRequestContext().getUri() %>&categoryfolder=<%= folder %></cms:link>"><b><cms:contentshow element="Title" /></b></a><br>
</p>

<div style="margin-bottom: 8px;"><p style="margin-top: 0px;">
<cms:contentcheck ifexists="FaqParagraph/Subtitle">
	<b><cms:contentshow element="FaqParagraph/Subtitle" /></b><br>
</cms:contentcheck>
<cms:contentshow element="FaqParagraph/Text" />
</p>

<%-- begin images --%>
<cms:contentcheck ifexists="FaqParagraph/Image">
<cms:contentloop element="FaqParagraph/Image">
<p><img src="<cms:link><cms:contentshow element="Image" /></cms:link>" border="0" alt="<cms:contentshow element="Description" />"><br clear="all"><small><cms:contentshow element="Description" /></small></p>
</cms:contentloop>
</cms:contentcheck>
<%-- end images --%>

<%-- begin external links --%>
<cms:contentcheck ifexistsone="FaqParagraph/ExternalLink">
<p><fmt:message key="detail.label.FaqArticle.ExternalLink" />:</p>
<ul>
<cms:contentloop element="FaqParagraph/ExternalLink">
<c:set var="linkTarget">
	<cms:contentshow element="NewWin" />
</c:set>
<c:set var="desc">
	<cms:contentshow element="Description" />
</c:set>
<c:set var="href">
	<cms:contentshow element="Href" />
</c:set>
<li><%= cms.getAnchor("href", "desc", "linkTarget") %></li>
</cms:contentloop>
</ul>
</cms:contentcheck>
<%-- end external links --%>
</div>
</cms:contentload><%--
--%></c:when><%--

--%><c:otherwise>
<p><b><fmt:message key="item.noentries" /></b></p>
</c:otherwise></c:choose></fmt:bundle>