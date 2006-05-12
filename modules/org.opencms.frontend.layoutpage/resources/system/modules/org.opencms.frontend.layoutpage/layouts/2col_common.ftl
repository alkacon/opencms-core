<!-- Macros for 2 column common layout -->

<!-- Start of macros surrounding the paragraphs -->

<!-- Outer content start -->
<#macro content_start>
<table border="0" cellpadding="3" cellspacing="8" style="width: 100%;">
</#macro>

<!-- Outer content end -->
<#macro content_end>
</table>
</#macro>

<!-- Content row start -->
<#macro row_start>
<tr>
</#macro>

<!-- Content row end -->
<#macro row_end>
</tr>
</#macro>

<!-- Paragraph element start -->
<#macro element_start>
<td style="width: 50%; vertical-align: top;">
</#macro>

<!-- Paragraph element end -->
<#macro element_end>
</td>
</#macro>


<#-- Start of macros for the different image links on images and descriptions -->

<#macro image_without_link>
${image}
</#macro>

<#macro image_with_link>
<a href="#" onclick="${target}" title="${title}">${image}</a>
</#macro>

<#macro description_without_link>
${description}
</#macro>

<#macro description_with_link>
<a href="#" onclick="${target}" title="${title}">${description}</a>
</#macro>


<#-- Start of macros for the different paragraph layouts -->

<#macro imagebottom>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p>
${text}<br>
${image}
</div></p>
</#macro>

<#macro imagebottom_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p>
${text}<br>
${image}
<span style="display: block; font-size: 11px;">${description}</span>
</div></p>
</#macro>

<#macro imageleft>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p><span style="float: left; padding: 0 5px 5px 2px; font-size: 11px;">${image}</span>
${text}</p></div>
<div style="clear: both;"></div>
</#macro>

<#macro imageleft_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p><span style="width: ${imagewidth}px; float: left; padding: 0 5px 5px 2px; font-size: 11px;">${image}
${description}</span>
${text}</p></div>
<div style="clear: both;"></div>
</#macro>

<#macro imageleft_textright>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td style="padding-right: 8px; vertical-align: top;">${image}</td>
	<td style="vertical-align: top;">${text}</td>
</tr>
</table>
</#macro>

<#macro imageleft_textright_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td style="padding-right: 8px; vertical-align: top; width: ${imagewidth}px;">${image}<span style="display: block; font-size: 11px;">${description}</span></td>
	<td style="vertical-align: top;">${text}</td>
</tr>
</table>
</#macro>

<#macro imageright>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p><span style="float: right;	padding: 0 2px 5px 5px;	font-size: 11px;">${image}</span>
${text}</p></div>
<div style="clear: both;"></div>
</#macro>

<#macro imageright_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p><span style="width: ${imagewidth}px; float: right; padding: 0 2px 5px 5px; font-size: 11px;">${image}
${description}</span>
${text}</p></div>
<div style="clear: both;"></div>
</#macro>

<#macro imageright_textleft>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td style="vertical-align: top;">${text}</td>
	<td style="padding-left: 8px; vertical-align: top;">${image}</td>
</tr>
</table>
</#macro>

<#macro imageright_textleft_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td style="vertical-align: top;">${text}</td>
	<td style="padding-left: 8px; vertical-align: top; width: ${imagewidth}px;">${image}<span style="display: block; font-size: 11px;">${description}</span></td></tr>
</table>
</#macro>

<#macro imagetop>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
${image}<br clear="all"/>
${text}
</#macro>

<#macro imagetop_desc>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
${image}<br clear="all"/>
<span style="display: block; font-size: 11px;">${description}</span>
${text}
</#macro>

<#macro textonly>
<#if headline != "">
<h2 style="margin-top: 0; padding-top: 0;">${headline}</h2>
</#if>
<div style="width: 100%;"><p>${text}</p></div>
</#macro>