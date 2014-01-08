<%@taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="closelink"><cms:link>/system/workplace/views/explorer/explorer_files.jsp</cms:link></c:set>
<cms:include file="/system/modules/org.opencms.ade.publish/publish.jsp">
	<cms:param name="closelink" value="${closelink}" />
</cms:include>

