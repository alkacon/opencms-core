<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />

<div class="element">

${cms:vfs(pageContext).xml["/xmlcontentdemo/list_content-scriptlet.html"].value["header-el"]}

<cms:contentload collector="%(property.collector)" param="%(property.xmlcontent-demo)article_%(number).html|article" editable="true">
<cms:contentaccess var="content" />

<div class="element">

<h3>${content.value['Title']}</h3>
<p>
${content.value['Teaser']}<br />
<small>
By ${content.value['Author']}
<a href="<cms:link>${content.filename}</cms:link>">read more...</a><br />
</small>
</p><p>
Article was created: ${cms:convertDate(content.file.dateCreated)}<br />
Current OpenCms URI: ${cms:getCmsObject(pageContext).requestContext.uri}<br />
Title read from property: ${cms:vfs(pageContext).property[content.filename]['Title']}<br />
</p>
<p>
<b>Excerpt:</b>
${cms:trimToSize(cms:stripHtml(content.value['Text']), 250)}
</p>
</div>

</cms:contentload>

${cms:vfs(pageContext).xml["/xmlcontentdemo/list_content-scriptlet.html"].value["footer-el"]}

</div>

<cms:include property="template" element="foot" />


