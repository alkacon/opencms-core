<%@ page session="false" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<cms:include property="template" element="head" />


<div class="element">

<h1>Test for the use of &lt;cms:include&gt; with XML content</h1>

<p>This simple example demonstrates how to use the template tags from the &lt;cms:&gt; taglib to access XML content directly. 
You must look at the page source code to understand this example fully. 
</p><p>
Please note: You should use the new &lt;cms:content...&gt; tags, not the &lt;cms:include&gt; tag to
access the XML content elements. This example is just to prove it works.
</p>

</div>

<div class="element">

<h1><cms:include file="article_0001.html" element="Title" /></h1>

<p><small>By <cms:include file="article_0001.html" element="Author" /></small></p>

<p><i><cms:include file="article_0001.html" element="Teaser" /></i></p>

<p>
<cms:include file="article_0001.html" element="Text" />
</p>

</div>


<cms:include property="template" element="foot" />


