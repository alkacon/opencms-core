<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<cms:contentload collector="singleFile" param="opencms:uri" editable="true">

<%@ include file="detail_include.txt" %>

</cms:contentload>

<cms:include property="template" element="foot" />


