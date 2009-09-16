<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)" editable="auto">
<div class="box box_schema1">
		<h4><cms:contentshow element="Name" /></h4>
<div class="boxbody">	
		<div style="font-style:italic"><cms:contentshow element="ShortDescription" /></div><br/>
		
		<!-- Optional links of the item -->
		<cms:contentcheck ifexists="Links">
			<ul>
				<cms:contentloop element="Links">
					<c:set var="newWindow"><cms:contentshow element="URI" /></c:set>
					<li><a href="<cms:link><cms:contentshow element="URI" /></cms:link>" <c:if test="${newWindow}">target="_blank"</c:if>>
						<cms:contentcheck ifexists="Description">
							<c:set var="desc"><cms:contentshow element="Description" /></c:set>
						</cms:contentcheck>
						<c:choose>
							<c:when test="${!empty desc}"><c:out value="${desc}" /></c:when>
							<c:otherwise><cms:contentshow element="URI" /></c:otherwise>
						</c:choose>
					</a></li>
				</cms:contentloop>
			</ul>
		</cms:contentcheck>
	</div>
	</div>
</cms:contentload>
