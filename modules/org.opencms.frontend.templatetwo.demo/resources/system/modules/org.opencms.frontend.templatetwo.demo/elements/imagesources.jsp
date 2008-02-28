<%@ page import="org.opencms.jsp.*, org.opencms.file.*, org.opencms.file.types.*, java.util.*" %> 
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
	CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
	CmsObject cms = jsp.getCmsObject();

	List nav = new ArrayList();
	CmsResourceFilter allImages = CmsResourceFilter.DEFAULT.addRequireType(CmsResourceTypeImage.getStaticTypeId());

	for (Iterator i = cms.getResourcesInFolder(jsp.getRequestContext().getFolderUri(), allImages).iterator(); i.hasNext();) {
		nav.add(CmsJspNavBuilder.getNavigationForResource(cms, jsp.getRequestContext().getSitePath((CmsResource)i.next())));
	}
	pageContext.setAttribute("nav", nav);
%>

<div class="box ${param.schema}">
	<h4>Bilder</h4>
	<div class="boxbody">
		<c:forEach items="${nav}" var="navElement">
		<p>
			<c:set var="props" value="${navElement.properties}" />
			<h5><a name="${props['Title']}"></a>${props['Title']}<br/></h5>
			<c:out value="${props['Description']}" escapeXml="false" />
			<br /><br />
		</p>
		</c:forEach>
	</div>
</div>