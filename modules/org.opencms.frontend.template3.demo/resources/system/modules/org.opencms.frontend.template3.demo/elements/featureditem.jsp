<%@ page import="org.opencms.file.*,org.opencms.jsp.*,org.opencms.relations.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
  CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
%>
<cms:contentload collector="allInFolder" param="/demo_t3/_content/items/|t3item" preload="true">
	<cms:contentinfo var="info" />
	<c:set var="max" value="${info.resultSize}" />
	<c:set var="cur"><%= System.currentTimeMillis() % ((Integer)pageContext.getAttribute("max")).intValue() %></c:set>

	<c:set var="cnt" value="0" />
	<cms:contentload editable="true">
		<c:set var="file"><cms:contentshow element="%(opencms.filename)" /></c:set>
<%
  String category = CmsCategoryService.getInstance().readResourceCategories(jsp.getCmsObject(), pageContext.getAttribute("file").toString()).get(0).getName().toLowerCase();
  String link = "/demo_t3/dictionary/item_composite.html";
  if (category.contains("liliaceous")) {
    link = "/demo_t3/dictionary/item_liliaceous.html";
  } else if (category.contains("rosaceous")) {
    link = "/demo_t3/dictionary/item_rosaceous.html";
  } 
  link += "?id=" + jsp.getCmsObject().readResource(pageContext.getAttribute("file").toString()).getStructureId();
  pageContext.setAttribute("link", link);
%>
		<c:if test="${cnt == cur}">
			<cms:contentcheck ifexists="Images">
			<cms:contentloop element="Images">
				<c:set var="image"><cms:contentshow element="Image" /></c:set>
				<c:set var="imageName" value="${image}" />
				<c:if test="${fn:indexOf(image, '?') != - 1}">
					<c:set var="imageName" value="${fn:substringBefore(image, '?')}" />
				</c:if>
				<c:set var="imagetitle">${cms:vfs(pageContext).property[imageName]['Title']}</c:set>
				<c:set var="imagefolder"><%= CmsResource.getFolderPath((String)pageContext.getAttribute("imageName")) %></c:set>
				<div class="image">
					<a href="<cms:link>${link}</cms:link>">
						<cms:img scaleType="1" width="185" alt="${imagetitle}">
							<cms:param name="src">${image}</cms:param> 
						</cms:img>
					</a>
					<div class="description">
						<a href="<cms:link>${imagefolder}index.html#${imagetitle}</cms:link>">${imagetitle}</a><br />
						<cms:contentshow element="Description" />
					</div>
				</div>
			</cms:contentloop>
			</cms:contentcheck>
			<cms:contentcheck ifexistsnone="Images">
				<c:set var="cur" value="${cur+1}" />
			</cms:contentcheck>
		</c:if>
		<c:set var="cnt" value="${cnt+1}" />
	</cms:contentload>

</cms:contentload>