<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editor.*,
	org.opencms.jsp.*"
	buffer="none"
	session="false"%><%

// Create a JSP action element
org.opencms.jsp.CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
String uri = cms.getRequestContext().getUri();
CmsDialog wp = new CmsDialog(cms);

String editTarget = (String)request.getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_TARGET);
String editElement = (String)request.getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_ELEMENT);
String editLocale = (String)request.getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_LOCALE);
String editLink = cms.link("/system/workplace/jsp/editors/editor.html");

String editId = "directedit";

if (editTarget != null) {
	editId += "_" + editTarget.substring(editTarget.lastIndexOf("/")+1);
}
if (editElement != null) {
	editId += "_" + editElement;
}
%><%--

--%><cms:template element="start_directedit_enabled">
<!-- EDIT BLOCK START -->
<div id="<%= editId %>" class="directedit_norm">
<form name="form_<%= editId %>" method="post" action="<%= editLink %>" class="nomargin">
<input type="hidden" name="resource" value="<%= editTarget %>">
<input type="hidden" name="directedit" value="true">
<input type="hidden" name="elementlanguage" value="<%= editLocale %>">
<input type="hidden" name="elementname" value="<%= editElement %>">
<input type="hidden" name="backlink" value="<%= uri %>">
</form>
<span class="directedit_button" onmouseover="activate('<%= editId %>');" onmouseout="deactivate('<%= editId %>');">
<table border="0" cellpadding="1" cellspacing="0">
<tr>
	<%=wp.button("javascript:document.forms['form_" + editId + "'].submit();", null, "directedit", "editor.frontend.button.edit", 1)%></tr>
</table>
</span>
</cms:template><%--

--%><cms:template element="end_directedit_enabled">
</div>
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="start_directedit_disabled">
<!-- EDIT BLOCK START -->
<div id="<%= editId %>" class="directedit_norm">
<span class="directedit_button" onmouseover="activate('<%=editId%>');" onmouseout="deactivate('<%=editId%>');">
<table border="0" cellpadding="1" cellspacing="0">
<tr>
	<%=wp.button(null, null, "directedit", "editor.frontend.button.locked", 1)%></tr>
</table>
</span>
</cms:template><%--

--%><cms:template element="end_directedit_disabled">
</div>
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="start_directedit_inactive">
<!-- EDIT BLOCK START -->
</cms:template><%--

--%><cms:template element="end_directedit_inactive">
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="directedit_includes">
<style type="text/css">
<!--
a.button {
	color: #000000;
	text-decoration: none;
	cursor: hand;
}
a.button:active {
	color: #000000;
	text-decoration: none;
}
a.button:hover {
	color: #000000;
	text-decoration: none;
}
a.button:visited {
	color: #000000;
	text-decoration: none;
}
span.combobutton {
	display: block;
	height: 17px;
	font-family: verdana, sans-serif;
	font-size: 11px;
	white-space: nowrap;
	padding-top: 3px;
	padding-left: 21px;
	padding-right: 5px;
	background-repeat: no-repeat;
	background-color: #c0c0c0;
}
span.norm {
	display: block;
	border: 1px solid #c0c0c0;
}
span.over {
	display: block;
	border-top: 1px solid #ffffff;
	border-left: 1px solid #ffffff;
	border-bottom: 1px solid #777777;
	border-right: 1px solid #777777;
}
span.push {
	display: block;
	border-top: 1px solid #777777;
	border-left: 1px solid #777777;
	border-bottom: 1px solid #ffffff;
	border-right: 1px solid #ffffff;
}
span.disabled {
	display: block;
	border: 1px solid #c0c0c0;
	color: #888888;
}
div.directedit_norm {
	width: 100%;
	padding-top: 1px;
	padding-bottom: 1px;
}
div.directedit_over {
	width: 100%;
	padding-top: 0;
	padding-bottom: 0;
	background-color: #f0f0f0;
	border-top: 1px dotted #000000;
	border-bottom: 1px dotted #000000;
}
span.directedit_button {
	position: absolute;
	z-index: 25;
	background-color: #c0c0c0;
	filter:progid:DXImageTransform.Microsoft.Alpha(opacity=85, finishopacity=85, style=1);
}
form.nomargin {
	display: none;
}
//-->
</style>

<script type="text/javascript">
<!--
function activate(id) {
	var el = document.getElementById(id);
	if (el.className == "directedit_norm") {
		el.className = "directedit_over";
	}
}
function deactivate(id) {
	var el = document.getElementById(id);
	if (el.className == "directedit_over") {
		el.className = "directedit_norm";
	}
}
//-->
</script>
</cms:template>