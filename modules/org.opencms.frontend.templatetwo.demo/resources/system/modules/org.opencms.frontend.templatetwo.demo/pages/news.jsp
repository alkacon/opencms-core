<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<cms:include property="template" element="head" />

<cms:contentload collector="singleFile" param="%(opencms.uri)" editable="auto">

	<h2><cms:contentshow element="Title" /></h2>
	<h3><cms:contentshow element="Paragraph/Headline" /></h3>
	<p><cms:contentshow element="Paragraph/Text" /></p>

</cms:contentload>

<cms:include property="template" element="foot" />