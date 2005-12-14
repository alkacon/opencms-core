<%@ page session="false" import="org.opencms.jsp.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%

    // Create a JSP action element
    CmsJspXmlContentBean cms = new CmsJspXmlContentBean(pageContext, request, response);

%>
<cms:include property="template" element="head" />

<div class="element">

<cms:include file="list_content-scriptlet.html" element="header" editable="true"/> 

<%
I_CmsXmlContentContainer container = cms.contentload("${property.collector}", "${property.xmlcontent-demo}article_${number}.html|article", true);
while (container.hasMoreContent()) {
%>

<div class="element">

<h3><%= cms.contentshow(container, "Title") %></h3>

<p>
<%= cms.contentshow(container, "Teaser") %></br>
<small>
By <%= cms.contentshow(container, "Author") %>
<a href="<cms:link><%= cms.contentshow(container, "${opencms.filename}") %></cms:link>">read more...</a>
</small>
</p>
</div>

<% } %>

<cms:include file="list_content.html" element="footer" editable="true"/>

</div>

<cms:include property="template" element="foot" />


