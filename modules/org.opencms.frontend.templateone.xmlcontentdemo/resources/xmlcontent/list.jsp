<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<div class="element">

<cms:include file="list_content.html" element="header" editable="true"/> 

<cms:contentload collector="${property.collector}" param="${property.xmlcontent-demo}article_${number}.html|25" editable="true">

<div class="element">

<h3><cms:contentshow element="Title" /></h3>

<p>
<cms:contentshow element="Teaser" /></br>
<small>
By <cms:contentshow element="Author" />
<a href="<cms:link><cms:contentshow element="${opencms.filename}" /></cms:link>">read more...</a>
</small>
</p>
</div>

</cms:contentload>

<cms:include file="list_content.html" element="footer" editable="true"/>

</div>

<cms:include property="template" element="foot" />


