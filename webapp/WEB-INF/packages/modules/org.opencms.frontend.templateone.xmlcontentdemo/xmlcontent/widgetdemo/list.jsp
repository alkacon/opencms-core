<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<div class="element">

<cms:include file="list_content.html" element="header" editable="true"/>

<cms:contentload collector="allInFolderDateReleasedDesc" property="xmlcontent-demo" param="widgetdemo_${number}|13" editable="true">

<div class="element">

<cms:contentcheck ifexists="Image">
<a href='<cms:contentshow element="Link" />'><img src='<cms:link><cms:contentshow element="Image" /></cms:link>' border="0" align="right"></a>
</cms:contentcheck>
<h3><font color='<cms:contentshow element="Color" />'><cms:contentshow element="String" /></font></h3>

<p><cms:contentshow element="Html" /></p>

</div>

</cms:contentload>

</div>

<cms:include file="list_content.html" element="footer" editable="true"/>

<cms:include property="template" element="foot" />


