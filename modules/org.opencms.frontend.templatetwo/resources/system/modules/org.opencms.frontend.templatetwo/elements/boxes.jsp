<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<c:catch>
	<c:set var="content" value="${cms:vfs(pageContext).readXml[param.config]}" />
</c:catch>
<c:choose>
	<c:when test="${content.hasValue['Config']}">
		<c:set var="list" value="${content.valueList['Config/Element']}" />
	</c:when>
	<c:otherwise>
		<c:set var="list" value="${content.valueList['Element']}" />
	</c:otherwise>
</c:choose>

<c:forEach items="${list}" var="element">
	<c:if test="${param.orientation == element.value['Orientation']}">

		<c:set var="type" value="${cms:vfs(pageContext).readResource[element.value['File']].typeId}" />
		<c:choose>
			<%-- Text Boxes --%>
			<c:when test="${type == 72}">
				<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/boxes/textbox.jsp:52db3537-d890-11dc-8ec1-3bdd2ea0b1ac)">
					<cms:param name="file" value="${element.value['File']}" />
					<cms:param name="schema" value="${element.value['ColorSchema']}" />
				</cms:include>
			</c:when>
			<%-- List Boxes --%>
			<c:when test="${type == 73}">
				<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/boxes/listbox.jsp:cd31e53a-da20-11dc-b10e-3bdd2ea0b1ac)">
					<cms:param name="file" value="${element.value['File']}" />
					<cms:param name="schema" value="${element.value['ColorSchema']}" />
				</cms:include>
			</c:when>
			<%-- Link Boxes --%>
			<c:when test="${type == 76}">
				<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.templatetwo/elements/boxes/linkbox.jsp:5b09eab1-e3be-11dc-82b1-3bdd2ea0b1ac)">
					<cms:param name="file" value="${element.value['File']}" />
					<cms:param name="schema" value="${element.value['ColorSchema']}" />
				</cms:include>
			</c:when>
			<%-- Jsp Boxes --%>
			<c:when test="${type == 4}">
				<cms:include file="${element.value['File']}">
					<cms:param name="schema" value="${element.value['ColorSchema']}" />
				</cms:include>
			</c:when>
		</c:choose>
	</c:if>
</c:forEach>