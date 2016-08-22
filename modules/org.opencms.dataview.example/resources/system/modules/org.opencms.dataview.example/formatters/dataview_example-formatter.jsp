<%@page buffer="none" session="false" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<cms:formatter var="content">
<div style="min-height: 100px; border: 1px solid blue; ">
	Selected items:
	<c:forEach var="Item" items="${content.valueList.Item}">
		<hr>
		<c:out value="${Item.value.Id}"/> -- <c:out value="${Item.value.Title}" />
	</c:forEach>
</div>
</cms:formatter>