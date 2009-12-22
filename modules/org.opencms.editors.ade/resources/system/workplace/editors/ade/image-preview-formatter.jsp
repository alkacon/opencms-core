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
		font-size:14px;
		margin:3px 3px 6px;
		padding:0;
		position:absolute;
		/*display:inline-block;*/
	}
</style>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<div>
	<%-- <button class="ui-state-default ui-corner-all" name="showDetails">
		<span class="cms-galleries-button cms-galleries-icon-movedown cms-icon"> </span>
	</button>--%>
	<h3>${cms.title}</h3>
	<%-- <div>
		<b>Last modified:</b>&nbsp;${cms.resource.dateLastModified}<br />
		<b>Path:</b>&nbsp;${cms.path}<br />
	</div> --%>
	<div class="cms-image-preview"></div>	
	<input type="hidden" value='${cms.jsonForActiveImage}' >
	
</div>