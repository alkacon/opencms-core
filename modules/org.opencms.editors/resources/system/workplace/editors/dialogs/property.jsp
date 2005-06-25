<%@ page import="org.opencms.workplace.editors.*" %><%	

	// initialize the workplace class
	CmsDialogProperty wp = new CmsDialogProperty(pageContext, request, response);
	String additionalScript = "";
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialogProperty.ACTION_CLOSEPOPUP_SAVE:
//////////////////// ACTION: save edited properties for the current resource type and close the popup window

	wp.actionEdit(request);
	if (wp.hasTemplateChanged()) {
		// add script to refresh editor content when template has changed
		additionalScript = "function closeAction() {\n\tthis.refreshOpener = true;\n}\nvar closeObj = new closeAction();\nif (window.opener.popupCloseAction) {\nwindow.opener.popupCloseAction(closeObj);\n}";
	}


case CmsDialogProperty.ACTION_CLOSEPOPUP:
//////////////////// ACTION: close the popup window
%>
	
	<html><head></head>
	<script type="text/javascript">
		<%= additionalScript %>
		window.close();
	</script>
	</head></html>
	
<%
break;

case CmsDialogProperty.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed, close dialog

	wp.actionDeleteResource();
	wp.actionCloseDialog();

break;


case CmsDialogProperty.ACTION_SAVE_EDIT:
//////////////////// ACTION: save edited properties for the current resource type

	wp.actionEdit(request);
	wp.actionCloseDialog();

break;


case CmsDialogProperty.ACTION_EDIT:
default:
//////////////////// ACTION: show edit properties window

	wp.setParamAction(wp.DIALOG_SAVE_EDIT);
	
%><%= wp.htmlStart(null, wp.getParamTitle()) %>
<script type="text/javascript">
<!--

function resizeWindow() {
	var wantedHeight = document.body.offsetHeight + 20;
	if (wantedHeight > screen.availHeight) {
		wantedHeight = screen.availHeight;
	}
	window.resizeTo(document.body.offsetWidth + 10, wantedHeight);
}

function toggleDelete(propName) {
	var sepIndex = propName.indexOf("---");
	if (sepIndex != -1) {
		propName = propName.substring(0, sepIndex);
		var checked = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName).checked;
		var count = 1;
		var curElem = null;
		while (true) {
			curElem = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName+"---"+count);
			if (curElem == null) {
				break;
			}
			var hiddenValue = document.getElementById("<%= wp.PREFIX_HIDDEN %>"+propName+"---"+count).value;
			if (checked == true) {
				curElem.value = hiddenValue;
			} else {
				curElem.value = "";
			}
			count ++;
		}	
	} else {
		var hiddenValue = document.getElementById("<%= wp.PREFIX_HIDDEN %>"+propName).value;
		var checked = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName).checked;
		var field = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName);
		if (checked == true) {
			field.value = hiddenValue;
		} else {
			field.value = "";
		}
	}
	
}

function checkValue(propName) {
	var sepIndex = propName.indexOf("---");
	var allEmpty = true;
	var newVal = null;
	if (sepIndex != -1) {
		propName = propName.substring(0, sepIndex);
		var count = 1;
		var curElem = null;
		while (true) {
			curElem = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName+"---"+count);
			if (curElem == null) {
				break;
			}
			newVal = curElem.value;
			if (newVal != null && newVal != "") {
				allEmpty = false;
				break;
			}
			count ++;
		}	
	} else {
		newVal = document.getElementById("<%= wp.PREFIX_VALUE %>"+propName).value;
		if (newVal != null && newVal != "") {
			allEmpty = false;
		}
	}
	var field = document.getElementById("<%= wp.PREFIX_USEPROPERTY %>"+propName);
	if (allEmpty) {
		field.checked = false;
	} else {
		field.checked = true;
	}
}

function submitAdvanced() {
	document.forms["main"].action.value = "<%= wp.DIALOG_SHOW_DEFAULT %>";
	document.forms["main"].submit();
}

function toggleNav() {
	var checked = document.getElementById("enablenav").checked;
	var disableField = false;
	var inputStyle = "Window";
	if (checked == true && document.getElementById("enablenav").disabled == false) {
		if (document.getElementById("<%= wp.PREFIX_HIDDEN %>NavText")) {
			var hiddenValue = document.getElementById("<%= wp.PREFIX_HIDDEN %>NavText").value;
			if (hiddenValue != null && hiddenValue != "" && document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText")) {
				document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText").checked = true;
				toggleDelete('NavText');
			}		
		}	
	} else {
		disableField = true;
		inputStyle = "Menu";
		if (document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText")) {
			document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText").checked = false;
			toggleDelete('NavText');
		}
	}
	if (document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText")) {
		document.getElementById("<%= wp.PREFIX_USEPROPERTY %>NavText").disabled = disableField;
	}
	document.getElementById("<%= wp.PREFIX_VALUE %>NavText").style.backgroundColor = inputStyle;
	document.getElementById("navpos").style.backgroundColor = inputStyle;
	document.getElementById("<%= wp.PREFIX_VALUE %>NavText").disabled = disableField;
	document.getElementById("navpos").disabled = disableField;
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
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.buildEditForm() %>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancelAdvanced(null, null, "value=\""+wp.key("button.advanced")+"\" onclick=\"submitAdvanced();\"") %>
</form>

<%= wp.dialogEnd() %>

<script type="text/javascript">
<!--
toggleNav();
<%
if ("true".equals(wp.getParamIsPopup())) {
	%>resizeWindow();
<%
} 
%>//-->
</script>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>