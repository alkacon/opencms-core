<%@page import="org.opencms.file.*,org.opencms.jsp.*,org.opencms.workplace.*" %><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%
CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
CmsDialog wp = new CmsDialog(jsp);
pageContext.setAttribute("projectid", "" + wp.getSettings().getProject()); 
String frameUri = wp.getSettings().getFrameUris().get("body");
pageContext.setAttribute("closelink", frameUri); 
%><cms:include file="/system/workplace/commons/publish.jsp">
	<cms:param name="closelink" value="${closelink}" />
	<cms:param name="publishProjectId" value="${projectid}" />
</cms:include>