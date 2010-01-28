<%@ page import="org.opencms.workplace.galleries.*, org.opencms.file.*, org.opencms.xml.containerpage.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
CmsContainerPageFormatterHelper cms = new CmsContainerPageFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<h3 style="margin:5px;"><c:out value="${cms.title}" /></h3>
<div style="margin:10px; overflow:auto; height: 90%;">
<c:forEach var="container" items="${cms.containerPage.containers}">
<div>
<h4><c:out value="${container.value.name}" ></c:out></h4>
<c:forEach var="element" items="${container.value.elements}">
<p style="margin:5px;"><%
CmsResource res=cms.getCmsObject().readResource(((CmsContainerElementBean)pageContext.getAttribute("element")).getElementId());
CmsProperty titleProp=cms.getCmsObject().readPropertyObject(res, org.opencms.file.CmsPropertyDefinition.PROPERTY_TITLE, false);
%><%=titleProp.getValue() %><br /><%=cms.getCmsObject().getSitePath(res) %></p>
</c:forEach>
</div>
</c:forEach></div>