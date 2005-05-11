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

%><fmt:setLocale value="${locale}" /><%--
--%><fmt:bundle basename="org/opencms/frontend/templateone/modules/workplace"><%--

--%><cms:contentload collector="${param.collector}" param="${param.folder}news_${number}.html|news|${param.elementcount}" editable="true" pageSize="${param.count}" pageIndex="${param.pageIndex}" pageNavLength="10"><%--
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
[<a href="<cms:link><%= cms.getRequestContext().getUri() %>?pageIndex=<c:out value="${i}" /></cms:link>"><c:out value="${i}" /></a>]&nbsp;
</c:otherwise>
</c:choose>
</c:forEach>
</c:if>
<c:if test="${shownumber == 'true'}"></p></c:if>
</c:if>
<p>
<a href="<cms:link><cms:contentshow element="${opencms.filename}" />?uri=<%= cms.getRequestContext().getUri() %></cms:link>"><b><cms:contentshow element="Title" /></b></a><br>
<cms:contentcheck ifexists="Date">
<c:set var="dateString">
	<cms:contentshow element="Date" />
</c:set>
<%
	cms.setDate("dateString");
%>
<small><fmt:formatDate value="${date}" type="date" dateStyle="long"  /> - <cms:contentshow element="Teaser" /></small>
</cms:contentcheck>
<small><a href="<cms:link><cms:contentshow element="${opencms.filename}" />?uri=<%= cms.getRequestContext().getUri() %></cms:link>"><fmt:message key="item.readmore" /></a></small>
</p>
</cms:contentload><%--
--%></fmt:bundle>