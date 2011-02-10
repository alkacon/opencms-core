<%@page session="false" import="org.opencms.jsp.*,org.opencms.main.*,org.opencms.site.*,org.opencms.util.CmsRequestUtil"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%	
	//Create a JSP action element
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	//
%>


<cms:contentload collector="singleFile" param="%(opencms.uri)">
	<c:set var="lnkUri"><cms:contentshow element="Link" /></c:set>
	<c:set var="internal" value="false" />
	<c:if test="${fn:indexOf(lnkUri, '/') == 0}">
		<c:set var="internal" value="true" />
	</c:if>

	<c:set var="responsetype"><cms:contentshow element="Type" /></c:set>
	
	<%	String newurl=(String)pageContext.getAttribute("lnkUri"); 
		if (newurl.startsWith("/sites/")) {
			String siteRoot = OpenCms.getSiteManager().getSiteRoot(newurl);
			CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
			String sitePath = newurl;
			if (sitePath.startsWith(site.getSiteRoot())) {
               			sitePath = newurl.substring(site.getSiteRoot().length());
            		}
			newurl = site.getUrl() + sitePath;
			pageContext.setAttribute("lnkUri", newurl);
			pageContext.setAttribute("internal", new Boolean(false));
		}
	%>
	<c:choose>
		<c:when test="${responsetype == '301'}">
			<c:if test="${internal == true}">
				<%CmsRequestUtil.redirectPermanently(cms,newurl); %>
			</c:if>
			<c:if test="${internal == false}">
				<%
					cms.getRequest().setAttribute(
			            CmsRequestUtil.ATTRIBUTE_ERRORCODE,
			            new Integer(HttpServletResponse.SC_MOVED_PERMANENTLY));
					cms.getResponse().setHeader("Location", newurl);
					cms.getResponse().setHeader("Connection", "close");
				%>
			</c:if>
		</c:when>
		<c:otherwise>
			<c:if test="${internal == true}">
				<%CmsRequestUtil.redirectRequestSecure(cms,newurl); %>
			</c:if>
			<c:if test="${internal == false}">
				<c:redirect url="${lnkUri}"/>
			</c:if>
		</c:otherwise>
	</c:choose>
</cms:contentload>

