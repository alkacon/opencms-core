<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	org.opencms.i18n.*"
	buffer="none"
	session="false" %><%

// Create a JSP action element
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
String uri = cms.getRequestContext().getUri();
CmsDialog wp = new CmsDialog(cms);

String editTarget = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_TARGET);
String editElement = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_ELEMENT);
String editLocale = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_LOCALE);
String editButtonStyleParam = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_BUTTONSTYLE);
String editOptions = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_OPTIONS);
String editNewLink = request.getParameter(I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_NEWLINK);

String editAction = I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_EDIT;
String deleteAction = I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_DELETE;
String newAction = I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_NEW;

boolean showEdit = true;
boolean showDelete = false;
boolean showNew = false;

if (editOptions != null) {
	showEdit = (editOptions.indexOf(editAction) >= 0);
	showDelete = (editOptions.indexOf(deleteAction) >= 0);
	showNew = (editOptions.indexOf(newAction) >= 0);
} 

int editButtonStyle = 1;
try {
	editButtonStyle = Integer.parseInt(editButtonStyleParam);
} catch (Exception e) {}

String editLink = cms.link("/system/workplace/editors/editor.jsp");
String deleteLink = cms.link("/system/workplace/commons/delete.jsp");

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
<form name="form_<%= editId %>" id="form_<%= editId %>" method="post" action="<%= editLink %>" class="nomargin">
<input type="hidden" name="resource" value="<%= editTarget %>">
<input type="hidden" name="directedit" value="true">
<input type="hidden" name="elementlanguage" value="<%= editLocale %>">
<input type="hidden" name="elementname" value="<%= editElement %>">
<input type="hidden" name="backlink" value="<%= uri %>">
<input type="hidden" name="newlink">
<input type="hidden" name="closelink">
</form>
<span class="directedit_button" onmouseover="activate('<%= editId %>');" onmouseout="deactivate('<%= editId %>');">
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<% 
if (showEdit) { 
%>
	<td class="directedit"><a href="#" onclick="javascript:doSubmit('<%= editId %>', '<%= editAction %>');" class="button"><span unselectable="on" class="over" onmouseover="className='over'" onmouseout="className='over'" onmousedown="className='push'" onmouseup="className='over'"><%
   if (editButtonStyle == 1) { 
	%><span id="bt_<%= editId %>" unselectable="on" class="combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/directedit_cl.gif');">&nbsp;<%= wp.key("editor.frontend.button.edit") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="combobutton" style="padding-left: 4px;"><%= wp.key("editor.frontend.button.edit") %></span><%
   } else { 
	%><span id="bt_<%= editId %>" unselectable="on" class="combobutton" style="padding-left: 15px; padding-right: 1px; background-image: url('<%= wp.getSkinUri() %>buttons/directedit_cl.gif'); background-position: 0px 0px;">&nbsp;</span><%
   } %></span></a></td>
<% 
}
if (showDelete) {
%>

	<td class="directedit"><a href="#" onclick="javascript:doSubmit('<%= editId %>', '<%= deleteAction %>');" class="button"><span unselectable="on" class="over" onmouseover="className='over'" onmouseout="className='over'" onmousedown="className='push'" onmouseup="className='over'"><%
   if (editButtonStyle == 1) { 
	%><span id="del_<%= editId %>" unselectable="on" class="combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/deletecontent.gif');">&nbsp;<%= wp.key("button.delete") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="combobutton" style="padding-left: 4px;"><%= wp.key("button.delete") %></span><%
   } else { 
	%><img class="button" border="0" src="<%= wp.getSkinUri() %>buttons/deletecontent.gif"><%
   } %></span></a></td>   
<%
}
if (showNew) {
%>   
	<td class="directedit"><a href="#" onclick="javascript:doSubmit('<%= editId %>', '<%= newAction %>', '<%= editNewLink %>');" class="button"><span unselectable="on" class="over" onmouseover="className='over'" onmouseout="className='over'" onmousedown="className='push'" onmouseup="className='over'"><%
   if (editButtonStyle == 1) { 
	%><span id="new_<%= editId %>" unselectable="on" class="combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/wizard.gif');">&nbsp;<%= wp.key("button.new") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="combobutton" style="padding-left: 4px;"><%= wp.key("button.new") %></span><%
   } else { 
	%><img class="button" border="0" src="<%= wp.getSkinUri() %>buttons/wizard.gif"><%
   } %></span></a></td>     
<%
}
%>   
</tr>
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
	color: ButtonText;
	text-decoration: none;
	cursor: pointer;
}
a.button:active {
	color: ButtonText;
	text-decoration: none;
}
a.button:hover {
	color: ButtonText;
	text-decoration: none;
}
a.button:visited {
	color: ButtonText;
	text-decoration: none;
}
span.combobutton {
	display: block;
	font-family: Verdana, sans-serif;
	font-size: 11px;
	white-space: nowrap;
	padding-top: 4px;
	padding-left: 21px;
	padding-right: 4px;
	padding-bottom: 4px;
	background-repeat: no-repeat;
	background-color: ButtonFace;
	background-position: 4px 0px;
}
span.norm {
	display: block;
	border: 1px solid ButtonFace;
}
span.over {
	display: block;
	border-top: 1px solid ButtonHighlight;
	border-left: 1px solid ButtonHighlight;
	border-bottom: 1px solid ButtonShadow;
	border-right: 1px solid ButtonShadow;
}
span.push {
	display: block;
	border-top: 1px solid ButtonShadow;
	border-left: 1px solid ButtonShadow;
	border-bottom: 1px solid ButtonHighlight;
	border-right: 1px solid ButtonHighlight;
}
span.disabled {
	display: block;
	border: 1px solid ButtonFace;
	color: ButtonShadow;
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
	background-color: InfoBackground;
	border-top: 1px dotted ThreedDarkShadow;
	border-bottom: 1px dotted ThreedDarkShadow;
}
span.directedit_button {
	position: absolute;
	background-color: ButtonFace;
}
td.directedit {
	line-height: 12px;
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
	var bt = document.getElementById("bt_" + id);
	if (bt != null) {
		bt.style.backgroundImage = "url(<%= wp.getSkinUri() %>buttons/directedit_op.gif)";
	}

}
function deactivate(id) {
	var el = document.getElementById(id);
	if (el.className == "directedit_over") {
		el.className = "directedit_norm";
	}
	var bt = document.getElementById("bt_" + id);
	if (bt != null) {
		bt.style.backgroundImage = "url(<%= wp.getSkinUri() %>buttons/directedit_cl.gif)";
	}
}
function doSubmit(id, action, link) {
	var form = document.getElementById("form_" + id);
	if (form != null) {
		if (action == "<%= editAction %>") {
			form.submit();
			return;
		} else if (action == "<%= deleteAction %>") {
			form.action = "<%= deleteLink %>";
			form.closelink.value = "<%= cms.link(uri) %>";			
			form.submit();
			return;
		} else if (action == "<%= newAction %>") {		
			form.newlink.value = link;	
			form.submit();
			return;
		}
	}
	alert("Unknown form action [" + id + "/" + action + "]");
}

//-->
</script>
</cms:template>