<%@ page import="org.opencms.workplace.galleries.*, org.opencms.workplace.*, java.util.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
	CmsDefaultFormatterHelper cms = new CmsDefaultFormatterHelper(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<h3><c:out value="${cms.title}" /></h3>
<div style="overflow:auto; height: 330px;">
<p>
	<img src="${cms.iconPath }" />&nbsp;<c:out value="${cms.path}" />
</p>
</div>	