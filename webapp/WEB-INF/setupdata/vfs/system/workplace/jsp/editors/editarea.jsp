<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ page import="
	org.opencms.workplace.*,
	com.opencms.flex.jsp.*,
	com.opencms.*,
	org.opencms.*"
	buffer="none"
	session="false"%><%

// Create a JSP action element
com.opencms.flex.jsp.CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
String uri = cms.getRequestContext().getUri();
CmsDialog wp = new CmsDialog(cms);

String editTarget = (String)request.getAttribute("__editTarget");
String editBody = (String)request.getAttribute("__editBody");
String editBodyLanguage = "en";
String editLink = cms.link("/system/workplace/jsp/editors/editor.html");

String editId = "editarea";

if (editTarget != null) {
	editId += "_" + editTarget.substring(editTarget.lastIndexOf("/")+1);
}
if (editBody != null) {
	editId += "_" + editBody;
}
%><%--

--%><cms:template element="start_editarea_enabled">
<!-- EDIT BLOCK START -->
<span id="<%= editId %>" class="editarea_norm">
<form name="form_<%= editId %>" method="post" action="<%= editLink %>" class="nomargin">
<input type="hidden" name="resource" value="<%= editTarget %>">
<input type="hidden" name="directedit" value="true">
<input type="hidden" name="bodylanguage" value="<%= editBodyLanguage %>">
<input type="hidden" name="bodyname" value="<%= editBody %>">
<input type="hidden" name="backlink" value="<%= uri %>">
</form>
<span class="editarea_button" onmouseover="activate('<%= editId %>');" onmouseout="deactivate('<%= editId %>');">
<table border="0" cellpadding="1" cellspacing="0">
<tr>
	<%=wp.button("javascript:document.forms['form_" + editId + "'].submit();", null, "directedit", "editor.frontend.button.edit", 1)%></tr>
</table>
</span>
</cms:template><%--

--%><cms:template element="end_editarea_enabled">
</span>
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="start_editarea_disabled">
<!-- EDIT BLOCK START -->
<span id="<%= editId %>" class="editarea_norm">
<span class="editarea_button" onmouseover="activate('<%=editId%>');" onmouseout="deactivate('<%=editId%>');">
<table border="0" cellpadding="1" cellspacing="0">
<tr>
	<%=wp.button(null, null, "directedit", "editor.frontend.button.locked", 1)%></tr>
</table>
</span>
</cms:template><%--

--%><cms:template element="end_editarea_disabled">
</span>
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="start_editarea_inactive">
<!-- EDIT BLOCK START -->
</cms:template><%--

--%><cms:template element="end_editarea_inactive">
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="editarea_includes">
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
span.editarea_norm {
	width: 100%;
	padding-top: 1px;
	padding-bottom: 1px;
}
span.editarea_over {
	width: 100%;
	padding-top: 0;
	padding-bottom: 0;
	background-color: #f0f0f0;
	border-top: 1px dotted #000000;
	border-bottom: 1px dotted #000000;
}
span.editarea_button {
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
	if (el.className == "editarea_norm") {
		el.className = "editarea_over";
	}
}
function deactivate(id) {
	var el = document.getElementById(id);
	if (el.className == "editarea_over") {
		el.className = "editarea_norm";
	}
}
//-->
</script>
</cms:template>