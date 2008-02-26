<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<cms:contentload collector="singleFile" param="${param.file}" editable="true">
	<cms:contentaccess var="content" />
	<div class="box ${param.schema}">
		<h4><c:out value="${content.value['Title']}" /></h4>
		<div class="boxbody">
			<c:out value="${content.value['Content']}" escapeXml="false" />

			<c:if test="${content.hasValue['JspFile']}">
				<cms:include file="${content.value['JspFile']}">
					<cms:param name="box.uri" value="${param.file}" />
				</cms:include>
			</c:if>	
		</div>
	</div>
</cms:contentload>
