<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ page import="
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
String editLink = cms.link("/system/workplace/jsp/editors/editor.html");

editLink += "?resource=" + editTarget
+ "&directedit=true"
+ "&bodylanguage=en"
+ "&bodyname=" + editBody
+ "&backlink=" + uri;

String editId = "editarea";

if (editTarget != null) {
    editId += "_" + editTarget.substring(editTarget.lastIndexOf("/")+1);
}
if (editBody != null) {
    editId += "_" + editBody;
}
%>

<cms:template element="start_editarea_enabled">
<!-- EDIT BLOCK START -->
<table width="100%" border="0" cellpadding="0" cellspacing="0"> 
<tr><td id="<%=editId%>" class="editarea_norm">
<div class="editbutton" onmouseover="activate('<%=editId%>');" onmouseout="deactivate('<%=editId%>');">
<table border="0" cellpadding="2" cellspacing="0">
<tr><%=wp.button(editLink, null, "directedit", "editor.frontend.button.edit", 1)%></tr>
</table>
</div>
</cms:template>

<cms:template element="end_editarea_enabled">
</td></tr>
</table>
<!-- EDIT BLOCK END -->
</cms:template>

<cms:template element="start_editarea_disabled">
<!-- EDIT BLOCK START -->
<table width="100%" border="0" cellpadding="0" cellspacing="0"> 
<tr><td id="<%=editId%>" class="editarea_norm">
<div class="editbutton" onmouseover="activate('<%=editId%>');" onmouseout="deactivate('<%=editId%>');">
<table border="0" cellpadding="2" cellspacing="0">
<tr><%=wp.button(null, null, "directedit", "editor.frontend.button.locked", 1)%></tr>
</table>
</div>
</cms:template>

<cms:template element="end_editarea_disabled">
</td></tr>
</table>
<!-- EDIT BLOCK END -->
</cms:template>

<cms:template element="start_editarea_inactive">
<!-- EDIT BLOCK START -->
</cms:template>

<cms:template element="end_editarea_inactive">
<!-- EDIT BLOCK END -->
</cms:template>

<cms:template element="editarea_includes">
<style type="text/css">
<!--
/* Button - Link (href) style */
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
/* Button - Combined image and text button wrapper */
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
/* Button - Normal style (inactive) */
span.norm {
    display: block; 
    border: 1px solid #c0c0c0;
}
/* Button - Hover style */
span.over {
    display: block; 
    border-top: 1px solid #ffffff;
    border-left: 1px solid #ffffff;
    border-bottom: 1px solid #777777;
    border-right: 1px solid #777777;
}

/* Button - Push style */
span.push {
    display: block;
    border-top: 1px solid #777777;
    border-left: 1px solid #777777;
    border-bottom: 1px solid #ffffff;
    border-right: 1px solid #ffffff;    
}

/* Button - Disabled style */
span.disabled {
    display: block;
    border: 1px solid #c0c0c0;
    color: #888888;
}
.editarea_norm {
    margin: 0;
    padding-left: 0;
    padding-right: 0;
    padding-top: 1px;
    padding-bottom: 1px;    
}
.editarea_over {
    width: 100%;
    margin: 0;
    padding: 0;
    background-color: #f0f0f0;
    border-top: 1px dotted #000000;
    border-bottom: 1px dotted #000000;
}
.editbutton {
    z-index: 25;
    position: absolute;
    background-color: #c0c0c0;
    filter:progid:DXImageTransform.Microsoft.Alpha(opacity=85, finishopacity=85, style=1);
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