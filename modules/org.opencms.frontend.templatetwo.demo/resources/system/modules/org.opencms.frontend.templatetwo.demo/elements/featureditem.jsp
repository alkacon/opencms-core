<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<cms:contentload collector="allInFolder" param="${param.path}|ttitem" editable="false" preload="true">
	<cms:contentinfo var="info" />
	<c:set var="max" value="${info.resultSize}" />
	<c:set var="cur"><%= System.currentTimeMillis() % ((Integer)pageContext.getAttribute("max")).intValue() %></c:set>

	<c:set var="cnt" value="0" />
	<cms:contentload>
		<c:set var="file"><cms:contentshow element="%(opencms.filename)" /></c:set>
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
					<a href="<cms:link>${file}</cms:link>">
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