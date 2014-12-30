<%@page taglibs="c,cms" import="org.opencms.file.*,org.opencms.jsp.*,org.opencms.workplace.*" %><%
CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
CmsDialog wp = new CmsDialog(jsp);
pageContext.setAttribute("projectid", "" + wp.getSettings().getProject()); 
String frameUri = wp.getSettings().getFrameUris().get("body");
pageContext.setAttribute("closelink", frameUri); 
%><cms:include file="/system/modules/org.opencms.ade.publish/publish.jsp">
	<cms:param name="closelink" value="${closelink}" />
	<cms:param name="publishProjectId" value="${projectid}" />
</cms:include>