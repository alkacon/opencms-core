<%@ page session="false" import="org.opencms.jsp.*" %><%--
--%><%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

// get currently active locale to initialize message bundle
String locale = cms.getRequestContext().getLocale().toString();
pageContext.setAttribute("locale", locale);

%><fmt:setLocale value="${locale}" /><%--
--%><fmt:bundle basename="org/opencms/frontend/templateone/modules/workplace"><%--

--%><cms:contentload collector="allInFolderDateReleasedDesc" property="style_news_articles" param="news_${number}.html|30|4" editable="true">

<div class="element2">
<b><cms:contentshow element="Title" /></b><br>
<small><a href="<cms:link><cms:contentshow element="opencms:filename" /></cms:link>"><fmt:message key="newsarticle.readmore" /></a></small>
</div>

</cms:contentload><%--
--%></fmt:bundle>