<%@ page session="false" import="org.opencms.jsp.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%

    // Create a JSP action element
    CmsJspXmlContentBean cms = new CmsJspXmlContentBean(pageContext, request, response);

%>
<cms:include property="template" element="head" />


<div class="element">

<%
I_CmsXmlContentContainer container = cms.contentload("%(elementProperty.collector)", "%(opencms.uri)", true);
while (container.hasMoreContent()) {
%>

<h1><%= cms.contentshow(container, "Title") %> (with SCRIPTLET)</h1>

<p><small>By <%= cms.contentshow(container, "Author") %></small></p>

<%
I_CmsXmlContentContainer loop = cms.contentloop(container, "Teaser");
while (loop.hasMoreContent()) {
%>
<p><i><%= cms.contentshow(loop) %></i></p>
<% } %>

<p>
<%= cms.contentshow(container, "Text") %>
</p>

</div>

<% } %>


<div class="element">

<cms:include file="detail_content.html" element="footer" editable="true"/>

</div>

<cms:include property="template" element="foot" />
