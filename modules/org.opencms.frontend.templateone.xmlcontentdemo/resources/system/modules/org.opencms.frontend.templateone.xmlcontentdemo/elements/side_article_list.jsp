<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>

<cms:contentload collector="allInSubTreeDateReleasedDesc" property="xmlcontent-side-uri" param="/article_${number}.html|article|4" editable="true">

<div class="element2">

<b><cms:contentshow element="Title" /></b><br>
<cms:contentshow element="Teaser" /><br>
<small><a href="<cms:link><cms:contentshow element="${opencms.filename}" /></cms:link>">read more...</a></small>

</div>

</cms:contentload>