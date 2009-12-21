<%@ page import="org.opencms.workplace.editors.sitemap.*"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %>
<% 
CmsDefaultFormatterHelper cms = new CmsDefaultFormatterHelper(pageContext, request, response);
pageContext.setAttribute("cms", cms);
%>
<fmt:setLocale value="${cms:vfs(pageContext).requestContext.locale}" />
<fmt:bundle basename="org.opencms.workplace.editors.sitemap.messages">
<div class="ui-corner-all cms-sitemap-item ui-state-default">
  <div class="cms-sitemap-entry-header ui-state-hover ui-corner-all"  >
  <a class="cms-left ui-icon cms-icon-triangle"></a>
  <h3>${cms.entry.title}</h3>
  </div>
  <div class="cms-additional">
    <div class="cms-additional-item cms-editable-field" rel="url-name">
    	<span class="cms-additional-item-title cms-item-title"><fmt:message key="GUI_DEFAULT_FORMATTER_URL_NAME_0" /></span> <%--
    	--%><span class="cms-additional-item-value cms-item-edit cms-url-name" alt="${cms.entry.name}">${cms:sitemap(pageContext).name}</span>
    </div>
    <div class="cms-additional-item" rel="url">
        <span class="cms-additional-item-title"><fmt:message key="GUI_DEFAULT_FORMATTER_URL_0" /></span><%--
    --%><span class="cms-additional-item-value cms-url"></span>
    </div>
    <div class="cms-additional-item" rel="path">
        <span class="cms-additional-item-title"><fmt:message key="GUI_DEFAULT_FORMATTER_VFSPATH_0" /></span><%--
    --%><span class="cms-additional-item-value cms-vfs-path" alt="${cms.path}">${cms.path}</span>
    </div>
  </div>
</div>
</fmt:bundle>
