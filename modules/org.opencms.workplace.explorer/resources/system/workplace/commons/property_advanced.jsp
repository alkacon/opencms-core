<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsPropertyAdvanced wp = new CmsPropertyAdvanced(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsPropertyAdvanced.ACTION_CLOSEPOPUP_SAVE:
//////////////////// ACTION: save edited properties for the current resource type and close the popup window

	wp.actionEdit(request);


case CmsPropertyAdvanced.ACTION_CLOSEPOPUP:
//////////////////// ACTION: close the popup window
%>
	
	<html><head></head>
	<script type="text/javascript">
		window.close();
	</script>
	</head></html>
	
<%
break;


case CmsPropertyAdvanced.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionDeleteResource();
	wp.actionCloseDialog();

break;


case CmsPropertyAdvanced.ACTION_SAVE_EDIT:
//////////////////// ACTION: save edited properties for the current resource type and close dialog

	wp.actionEdit(request);
	wp.actionCloseDialog();

break;


case CmsPropertyAdvanced.ACTION_SAVE_DEFINE:
//////////////////// ACTION: save new property definition for the current resource type
	
	wp.actionDefine();

break;


case CmsPropertyAdvanced.ACTION_SHOW_DEFINE:
//////////////////// ACTION: show define properties window, but first save changed property values
	
	wp.actionEdit(request);
	wp.setParamAction(wp.DIALOG_SAVE_DEFINE);
	
%><%= wp.htmlStart("help.explorer.contextmenu.properties", wp.getParamTitle()) %>
<script type="text/javascript">
<!--
function checkName() {
	var newProp = document.getElementById("<%= wp.PARAM_NEWPROPERTY %>").value;
	if (newProp != null && newProp != "") {
		document.getElementsByName("ok")[0].disabled = false;
	} else {
		document.getElementsByName("ok")[0].disabled = true;
	}
}
//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onSubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.newpropertydef") %></td>
	<td class="maxwidth"><input id="<%= wp.PARAM_NEWPROPERTY %>" name="<%= wp.PARAM_NEWPROPERTY %>" type="text" value="" class="maxwidth" onKeyup="checkName();" on></td>
</tr>
</table>

<%= wp.dialogSubheadline(wp.key("input.activepropertydef")) %>

<%= wp.dialogWhiteBoxStart() %>
<%= wp.buildActivePropertiesList() %>
<%= wp.dialogWhiteBoxEnd() %>

<%= wp.dialogContentEnd() %>
<% wp.setParamAction(wp.DIALOG_SHOW_DEFAULT); %>
<%= wp.dialogButtonsOkCancel("disabled=\"disabled\"", "onclick=\"location.href='" + wp.getDialogUri() + "?" + wp.paramsAsRequest() + "';\"") %>
</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;


case CmsPropertyAdvanced.ACTION_DEFAULT:
case CmsPropertyAdvanced.ACTION_SHOW_EDIT:
default:
//////////////////// ACTION: show edit properties window (default)

wp.setParamAction(wp.DIALOG_SAVE_EDIT);
	
%><%= wp.htmlStart("help.explorer.contextmenu.properties") %>
<script type="text/javascript">
<!--

function resizeWindow() {
	var wantedHeight = document.body.offsetHeight + 20;
	if (wantedHeight > screen.availHeight) {
		wantedHeight = screen.availHeight;
	}
	window.resizeTo(document.body.offsetWidth + 10, wantedHeight);
}

// global members needed for delayed setting of field values when clicking a checkbox
m_field = null;
m_newValue = null;

// called when using the checkbox, removes or adds content from/to input field
function toggleDelete(propName, prefix, activeTab) {
	var hiddenValue = document.getElementById(prefix+propName).value;
	var checked = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName).checked;
	var field = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName);
	field.value = "";
	if (checked == true) {
		// box has been checked, check style and get value from hidden field
		if (activeTab == "<%= wp.key(wp.PANEL_STRUCTURE) %>" && field.className == "dialogmarkedfield") {
			field.className = "maxwidth";
		}
		m_field = field;
		m_newValue = hiddenValue;
		// set the value delayed to avoid display issues
		setTimeout("setValueDelayed()", 20);
	} else {
		// box has been unchecked 
		var resValue = document.getElementById("<%= wp.PREFIX_RESOURCE %>"+propName).value;
		if (activeTab == "<%= wp.key(wp.PANEL_STRUCTURE) %>" && resValue.length > 0) {
			// in "shared properties" form, show resource value if present			
			field.className = "dialogmarkedfield";
			m_field = field;
			m_newValue = resValue;
			// set the value delayed to avoid display issues
			setTimeout("setValueDelayed()", 20);
		} else {
			// otherwise, clear input field
			field.value = "";
		}
	}
}

// necessary to toggle the value delayed because of display issues wih long property values
function setValueDelayed() {
	m_field.value = m_newValue;
}

// checks the value of an input field and (de)activates the checkbox, if necessary
function checkValue(propName, activeTab) {
	var newVal = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName).value;
	var checkfield = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName);
	if (newVal != null && newVal != "") {
		if (checkfield != null) {
			checkfield.checked = true;
		}
	} else {
		if (checkfield != null) {
			checkfield.checked = false;
		}
	}
}

// called on the onFocus event of an input field
function deleteResEntry(propName, activeTab) {
	var field = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName);
	if (activeTab == "<%= wp.key(wp.PANEL_STRUCTURE) %>" && field.className == "dialogmarkedfield") {
		// clear field to allow individual input
		field.value = "";
		field.className = "maxwidth";
	}
}

// called on the onBlur event of an input field
function checkResEntry(propName, activeTab) {
	if (activeTab == "<%= wp.key(wp.PANEL_STRUCTURE) %>") {
		// check only in "shared properties" form
		var newVal = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName).value;
		var resVal = document.getElementById("<%= wp.PREFIX_RESOURCE %>"+propName).value;
		var checkfield = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName);
		if (newVal == "" && resVal != "") {
			// new value is empty and resource value present, show resource value 
			document.getElementById("<%= wp.PREFIX_VALUE %>"+propName).value = resVal;
			document.getElementById("<%= wp.PREFIX_VALUE %>"+propName).className = "dialogmarkedfield";
			if (checkfield != null) {
				checkfield.checked = false;
			}			
		}
	}
}

function definePropertyForm() {
	document.main.<%= wp.PARAM_ACTION %>.value = "<%= wp.DIALOG_SHOW_DEFINE %>";
	document.main.submit();
}

// sets the form values, this function has to be called delayed because of display issues with large property values
// use the setTimeout function in the onload attribute in the page <body> tag to set the form values
function doSet() {
<%= wp.buildSetFormValues() %>
}

//-->
</script>
<%= wp.bodyStart("dialog", "onload=\"window.setTimeout('doSet()',50);\"") %>

<%= wp.dialogStart() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<%= wp.dialogTabContentStart(wp.getParamTitle(), "id=\"tabcontent\"") %>

<%= wp.buildEditForm() %>

<%= wp.dialogTabContentEnd() %>
<%= wp.dialogButtonsOkCancelDefine() %>
</form>

<%= wp.dialogEnd() %>
<%
if ("true".equals(wp.getParamIsPopup())) {
	%><script type="text/javascript">
<!--
resizeWindow();
//-->
</script>
<%
} 
%><%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>