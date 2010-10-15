<%@ page import="org.opencms.file.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<cms:contentload collector="singleFile" param="%(opencms.element)">
<div>
	<cms:contentaccess var="content" scope="page" />
	<h2><cms:contentshow element="Title" /></h2>
	<p><cms:contentshow element="Text" /></p>
	<p><fmt:formatDate value="${cms:convertDate(content.value['Release'])}" dateStyle="LONG" type="date" /></p>
	<p><cms:contentshow element="Author" /></p>	
</div>
</cms:contentload>
