<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />


<div class="element">
<cms:contentload collector="singleFile" param="opencms:uri" editable="true">

<%@ include file="detail_include.txt" %>

</cms:contentload>
</div>

<cms:include property="template" element="foot" />


