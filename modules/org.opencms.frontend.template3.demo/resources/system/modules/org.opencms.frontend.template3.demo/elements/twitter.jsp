<%@ page import="org.opencms.jsp.*" %>
<%@ page import= "org.opencms.xml.containerpage.*" %>
<%@ page import="java.util.*"  %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<cms:contentload collector="singleFile" param="%(opencms.element)" >
	<cms:contentaccess var="content" />
	<div>
		<c:set var="background"><cms:elementsetting name="background" default="43c43f"/></c:set>
		<iframe name="twitter" src="<cms:link>/system/modules/org.opencms.frontend.template3.demo/elements/twitterFrame.jsp?title=${content.value['Title']}&amp;subtitle=${content.value['Subtitle']}&amp;channel=${content.value['Channel']}&amp;background=${background}</cms:link>" style="margin: 0; border: none;" height="340" width="228" ></iframe>
	</div>
</cms:contentload>