<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<div class="element">
<cms:include file="list_content.html" element="header" editable="true"/> 

<cms:contentload collector="allInFolderDateReleasedDesc" property="xmlcontent-demo" param="bookmark_${number}.html|14" editable="true">
<div class="element">

<cms:contentcheck ifexistsall="Title,Link">
<h3><a href='<cms:contentshow element="Link" />' target="_blank"><cms:contentshow element="Title" /></a><br><span style="font-size:xx-small;font-weight:normal">URI: <a href='<cms:contentshow element="Link" />' target="_blank"><cms:contentshow element="Link" /></a></span></h3>
</cms:contentcheck>
<cms:contentcheck ifexists="Title" ifexistsnone="Link">
<h3><cms:contentshow element="Title" /></h3>
</cms:contentcheck>

<cms:contentcheck ifexists="Description">
<p><cms:contentshow element="Description" /></p>
</cms:contentcheck>

<cms:contentcheck ifexists="Contact">
<strong>Contact information:</strong>
<pre>
<cms:contentshow element="Contact/Company" />
<cms:contentshow element="Contact/FirstName" /><cms:contentshow element="Contact/LastName" />
<cms:contentshow element="Contact/Street" />
<cms:contentshow element="Contact/ZIP" /><cms:contentshow element="Contact/City" />
<cms:contentshow element="Contact/Country" />
<cms:contentcheck ifexists="Contact/ContactData[1]"><cms:contentshow element="Contact/ContactData[1]" /></cms:contentcheck>
<cms:contentcheck ifexists="Contact/ContactData[2]"><cms:contentshow element="Contact/ContactData[2]" /></cms:contentcheck>
<cms:contentcheck ifexists="Contact/ContactData[3]"><cms:contentshow element="Contact/ContactData[3]" /></cms:contentcheck>
</pre>
</cms:contentcheck>

</div>
</cms:contentload>

</div>

<cms:include property="template" element="foot" />


