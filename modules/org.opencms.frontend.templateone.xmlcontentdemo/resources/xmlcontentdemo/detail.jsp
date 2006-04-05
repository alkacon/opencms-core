<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />


<div class="element">
<cms:contentload collector="${elementProperty.collector}" param="${opencms.uri}" editable="true">

<h1><cms:contentshow element="Title" /></h1>

<p><small>By <cms:contentshow element="Author" /></small></p>

<cms:contentloop element="Teaser">
<p><i><cms:contentshow /></i></p>
</cms:contentloop>

<p>
<cms:contentshow element="Text" />
</p>

</div>
</cms:contentload>


<div class="element">

<cms:include file="detail_content.html" element="footer" editable="true"/>

</div>

<cms:include property="template" element="foot" />


