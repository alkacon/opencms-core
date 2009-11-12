<%@ page import="org.opencms.workplace.galleries.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
org.opencms.workplace.galleries.CmsDefaultFormatterHelper cms = new org.opencms.workplace.galleries.CmsDefaultFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>

<div class="cms-list-item ui-widget-content ui-state-default ui-corner-all">
	<div style="background-image: url(${cms.galleryItem.icon});" class="cms-list-image"></div>
	<div class="cms-list-itemcontent">
    	<div class="cms-list-title">${cms.galleryItem.title}</div>
		<div class="cms-list-url">${cms.galleryItem.subtitle}</div>
	</div>
</div>

