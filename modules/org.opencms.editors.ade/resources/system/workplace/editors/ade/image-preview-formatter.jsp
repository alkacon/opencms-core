<%@ page import="org.opencms.workplace.galleries.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
org.opencms.workplace.galleries.CmsImageFormatterHelper cms = new org.opencms.workplace.galleries.CmsImageFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>
<style>
	.cms-image-preview {
		text-align: center;
	}
	
	h3 {
		margin: 3px;
	}
</style>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<div>
	<h3>${cms.title}</h3>
	<%-- <div>
		<b>Last modified:</b>&nbsp;${cms.resource.dateLastModified}<br />
		<b>Path:</b>&nbsp;${cms.path}<br />
	</div> --%>
	<div class="cms-image-preview"></div>	
	<input type="hidden" value='${cms.jsonForActiveImage}' >
	
</div>