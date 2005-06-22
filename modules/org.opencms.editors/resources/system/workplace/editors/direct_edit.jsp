<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%--
--%><%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	java.util.*,
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

// random direct edit id generation
Random rnd = (Random)request.getAttribute("__Random");
if (rnd == null) {
	rnd = new Random();
	request.setAttribute("__Random", rnd);
}
String editId = "directedit_".concat(String.valueOf(rnd.nextInt()));

%><%--

--%><cms:template element="start_directedit_enabled">
<!-- EDIT BLOCK START -->
<script type="text/javascript">
<!--
	registerButtonOcms("<%= editId %>");
//-->
</script>
<div class="ocms_de_bt" id="buttons_<%= editId %>">
<form name="form_<%= editId %>" id="form_<%= editId %>" method="post" action="<%= editLink %>" class="ocms_nomargin">
<input type="hidden" name="resource" value="<%= editTarget %>">
<input type="hidden" name="directedit" value="true">
<input type="hidden" name="elementlanguage" value="<%= editLocale %>">
<input type="hidden" name="elementname" value="<%= editElement %>">
<input type="hidden" name="backlink" value="<%= uri %>">
<input type="hidden" name="newlink">
<input type="hidden" name="closelink">
<input type="hidden" name="editortitle">
</form>
<span onmouseover="activateOcms('<%= editId %>');" onmouseout="deactivateOcms('<%= editId %>');">
<table border="0" cellpadding="0" cellspacing="0" id="table_<%= editId %>">
<tr>
<% 
if (showEdit) { 
%>
	<td class="ocms_de"><a href="#" onclick="javascript:submitOcms('<%= editId %>', '<%= editAction %>');" class="ocms_button"><span unselectable="on" class="ocms_over" onmouseover="className='ocms_over'" onmouseout="className='ocms_over'" onmousedown="className='ocms_push'" onmouseup="className='ocms_over'"><%
   if (editButtonStyle == 1) { 
	%><span id="bt_<%= editId %>" unselectable="on" class="ocms_combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/directedit_cl.png');">&nbsp;<%= wp.key("editor.frontend.button.edit") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="ocms_combobutton" style="padding-left: 4px;"><%= wp.key("editor.frontend.button.edit") %></span><%
   } else { 
	%><span id="bt_<%= editId %>" unselectable="on" class="ocms_combobutton" style="padding-left: 15px; padding-right: 1px; background-image: url('<%= wp.getSkinUri() %>buttons/directedit_cl.png'); background-position: 0px 0px;" title="<%= wp.key("editor.frontend.button.edit") %>">&nbsp;</span><%
   } %></span></a></td>
<% 
}
if (showDelete) {
%>

	<td class="ocms_de"><a href="#" onclick="javascript:submitOcms('<%= editId %>', '<%= deleteAction %>');" class="ocms_button"><span unselectable="on" class="ocms_over" onmouseover="className='ocms_over'" onmouseout="className='ocms_over'" onmousedown="className='ocms_push'" onmouseup="className='ocms_over'"><%
   if (editButtonStyle == 1) { 
	%><span id="del_<%= editId %>" unselectable="on" class="ocms_combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/deletecontent.png');">&nbsp;<%= wp.key("button.delete") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="ocms_combobutton" style="padding-left: 4px;"><%= wp.key("button.delete") %></span><%
   } else { 
	%><img border="0" src="<%= wp.getSkinUri() %>buttons/deletecontent.png" title="<%= wp.key("button.delete") %>" alt=""><%
   } %></span></a></td>   
<%
}
if (showNew) {
%>   
	<td class="ocms_de"><a href="#" onclick="javascript:submitOcms('<%= editId %>', '<%= newAction %>', '<%= editNewLink %>');" class="ocms_button"><span unselectable="on" class="ocms_over" onmouseover="className='ocms_over'" onmouseout="className='ocms_over'" onmousedown="className='ocms_push'" onmouseup="className='ocms_over'"><%
   if (editButtonStyle == 1) { 
	%><span id="new_<%= editId %>" unselectable="on" class="ocms_combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/new.png');">&nbsp;<%= wp.key("button.new") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="ocms_combobutton" style="padding-left: 4px;"><%= wp.key("button.new") %></span><%
   } else { 
	%><img border="0" src="<%= wp.getSkinUri() %>buttons/new.png" title="<%= wp.key("button.new") %>" alt=""><%
   } %></span></a></td>     
<%
}
%>   
</tr>
</table>
</span>
</div>
<div id="<%= editId %>" class="ocms_de_norm">
</cms:template><%--

--%><cms:template element="end_directedit_enabled">
</div>
<!-- EDIT BLOCK END -->
</cms:template><%--

--%><cms:template element="start_directedit_disabled">
<!-- EDIT BLOCK START -->
<script type="text/javascript">
<!--
	registerButtonOcms("<%= editId %>");
//-->
</script>
<div class="ocms_de_bt" id="buttons_<%= editId %>">
<span onmouseover="activateOcms('<%=editId%>');" onmouseout="deactivateOcms('<%=editId%>');">
<table border="0" cellpadding="0" cellspacing="0" id="table_<%= editId %>">
<tr>
	<td class="ocms_de"><span unselectable="on" class="ocms_disabled"><%
   if (editButtonStyle == 1) { 
	%><span unselectable="on" class="ocms_combobutton" style="background-image: url('<%= wp.getSkinUri() %>buttons/directedit_in.png');">&nbsp;<%= wp.key("editor.frontend.button.locked") %></span><%
   } else if (editButtonStyle == 2) { 
	%><span unselectable="on" class="ocms_combobutton" style="padding-left: 4px;"><%= wp.key("editor.frontend.button.locked") %></span><%
   } else { 
	%><img border="0" src="<%= wp.getSkinUri() %>buttons/directedit_in.png" title="<%= wp.key("editor.frontend.button.locked") %>" alt=""><%
   } %></span></td>
</table>
</span>
</div>
<div id="<%= editId %>" class="ocms_de_norm">
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
a.ocms_button,a.ocms_button:active,a.ocms_button:hover,a.ocms_button:visited {
	color: ButtonText;
	text-decoration: none;
	cursor: pointer;
}
span.ocms_combobutton {
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
span.ocms_norm {
	display: block;
	border: 1px solid ButtonFace;
}
span.ocms_over {
	display: block;
	border-top: 1px solid ButtonHighlight;
	border-left: 1px solid ButtonHighlight;
	border-bottom: 1px solid ButtonShadow;
	border-right: 1px solid ButtonShadow;
}
span.ocms_push {
	display: block;
	border-top: 1px solid ButtonShadow;
	border-left: 1px solid ButtonShadow;
	border-bottom: 1px solid ButtonHighlight;
	border-right: 1px solid ButtonHighlight;
}
span.ocms_disabled {
	display: block;
	border: 1px solid ButtonFace;
	color: ButtonShadow;
}
div.ocms_de_bt {
	visibility: hidden; 
	position: absolute; 
	z-index: 100;
}
div.ocms_de_norm {
	width: 100%;
	padding-top: 1px;
	padding-bottom: 1px;
}
div.ocms_de_over {
	width: 100%;
	padding-top: 0;
	padding-bottom: 0;
	background-color: InfoBackground;
	border-top: 1px dotted ThreedDarkShadow;
	border-bottom: 1px dotted ThreedDarkShadow;
}
td.ocms_de {
	line-height: 12px;
	background-color: ButtonFace;
}
form.ocms_nomargin {
	display: none;
	visibility: hidden;
}
//-->
</style>

<script type="text/javascript">
<!--
window.onresize = showButtonsOcms;
document.onkeyup = keyUpOcms;

var deButtonsOcms = new Array();
var ocms_btTimeout;
var visibleOcmsButtons = true;

function activateOcms(id) {
	var el = document.getElementById(id);
	if (el.className == "ocms_de_norm") {
		el.className = "ocms_de_over";
	}
	var bt = document.getElementById("bt_" + id);
	if (bt != null) {
		bt.style.backgroundImage = "url(<%= wp.getSkinUri() %>buttons/directedit_op.png)";
	}

}
function deactivateOcms(id) {
	var el = document.getElementById(id);
	if (el.className == "ocms_de_over") {
		el.className = "ocms_de_norm";
	}
	var bt = document.getElementById("bt_" + id);
	if (bt != null) {
		bt.style.backgroundImage = "url(<%= wp.getSkinUri() %>buttons/directedit_cl.png)";
	}
}
function submitOcms(id, action, link) {
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
			form.editortitle.value = "<%= wp.key("editor.title.new") %>";	
			form.newlink.value = link;	
			form.submit();
			return;
		}
	}
	alert("Unknown form action [" + id + "/" + action + "]");
}
function registerButtonOcms(id) {
	deButtonsOcms[deButtonsOcms.length] = id; 
	clearTimeout(ocms_btTimeout);
	ocms_btTimeout = setTimeout("showButtonsOcms()", 400);
}
function showButtonsOcms() {	
		
	var visStyle = "visible";
	if (!visibleOcmsButtons) {
		visStyle = "hidden";
	}
		
	for (var i = 0; i<deButtonsOcms.length; i++) {
		var btid = deButtonsOcms[i];
		var deDiv = document.getElementById(btid);
		var deButton = document.getElementById("buttons_" + btid);
		var deTable = document.getElementById("table_" + btid);
		var x = findPosXOcms(deDiv);
		var y = findPosYOcms(deDiv);
		var w1 = deDiv.offsetWidth; // width of surrounding div
		var w2 = deTable.offsetWidth; // width of button table
		
		x += (w1 - w2);		
		deButton.style.left = x + "px";
		deButton.style.top =  y + "px";				
		deButton.style.visibility = visStyle;
	}
}
function findPosXOcms(obj) {
    var curleft = 0; 
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curleft += obj.offsetLeft - obj.scrollLeft; 
            obj = obj.offsetParent; 
        } 
    } else if (obj.x) {
        curleft += obj.x; 
    }
    return curleft; 
}
function findPosYOcms(obj) {
    var curtop = 0; 
    if (obj.offsetParent) {
        while (obj.offsetParent) { 
            curtop += obj.offsetTop - obj.scrollTop; 
            obj = obj.offsetParent; 
        }
    } else if (obj.y) {
        curtop += obj.y;
    }
    return curtop;
}
function toggleVisibleOcms() {
	if (visibleOcmsButtons) {
		visibleOcmsButtons = false;
	} else {
		visibleOcmsButtons = true;
	}
	showButtonsOcms();
}
function keyUpOcms(evt) {
	if (!evt) {
		evt = window.event;
	}
	if ((evt.type == "keyup") && (evt.keyCode == 32) && evt.ctrlKey) {
		toggleVisibleOcms();
	}
}
//-->
</script>
</cms:template>