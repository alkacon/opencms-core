<%@ page import="org.opencms.frontend.templateone.*" buffer="none" session="false" %><%	

	// initialize the workplace class
	CmsPropertyTemplateOne wp = new CmsPropertyTemplateOne(pageContext, request, response);
	String additionalScript = "";
	
	// start of switch statement 
	
	switch (wp.getAction()) {
	
	case CmsPropertyTemplateOne.ACTION_CLOSEPOPUP_SAVE:
	//////////////////// ACTION: save edited properties for the current resource type and close the popup window

	wp.actionEdit(request);


	case CmsPropertyTemplateOne.ACTION_CLOSEPOPUP:
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

	case CmsPropertyTemplateOne.ACTION_CANCEL:
	//////////////////// ACTION: cancel button pressed, close dialog

	wp.actionCloseDialog();
	break;

	case CmsPropertyTemplateOne.ACTION_SAVE_EDIT:
	//////////////////// ACTION: save edited properties for the current resource type

	wp.actionEdit(request);
	wp.actionCloseDialog();

	break;


	case CmsPropertyTemplateOne.ACTION_EDIT:
	default:
	//////////////////// ACTION: show edit properties window

	wp.setParamAction(wp.DIALOG_SAVE_EDIT);
	
%>
	<%= wp.htmlStart(null, wp.getParamTitle()) %>
	<script type="text/javascript">
	<!--

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
		
		function toggleHeadImageProperties() {
		
			if(document.main.<%= wp.PREFIX_VALUE %>style_show_head_img[0].checked) {
				presetHeadImageProperties();				
			} else if(document.main.<%= wp.PREFIX_VALUE %>style_show_head_img[1].checked) {
				// enable fields
				toggleHeadImageFields(false, "Window");
				// set fieled values back
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value = document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_uri.value;
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value = document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_link.value;
			} else {				
				document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_uri.value = document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value;
				document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_link.value = document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value;
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value = "";
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value = "";
				toggleHeadImageFields(true, "Menu");	
			}			
		}	
		
		function toggleHeadImageFields(isDisabled, styleName) {
			document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.disabled = isDisabled;
			document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.disabled = isDisabled;
			document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.style.backgroundColor = styleName;
			document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.style.backgroundColor = styleName;
		}		
		
		function presetHeadImageProperties() {
		
			if(document.main.<%= wp.PREFIX_VALUE %>style_show_head_img[0].checked) {
				// overwrite hidden information only by switching from individual, otherwise the individual information goes lost
				if (document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value!='') {
					document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_uri.value = document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value;					
				}
				if (document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value!='')  {
					document.main.<%= wp.PREFIX_HIDDEN %>style_head_img_link.value = document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value;				
				}
				// always set default values
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_uri.value='<%=wp.getDefault("style_head_img_uri")%>';			
				document.main.<%= wp.PREFIX_VALUE %>style_head_img_link.value='<%=wp.getDefault("style_head_img_link")%>';
				// disable fields
				toggleHeadImageFields(true, "Menu");			
			} 			
		}	
		
		// sets the form values, this function has to be called delayed because of display issues with large property values
		// use the setTimeout function in the onload attribute in the page <body> tag to set the form values
		function doSet() {
			<%= wp.buildSetFormValues() %>
		}

//-->
</script>
<%= wp.bodyStart("dialog", "onload=\"toggleHeadImageProperties(); window.setTimeout('doSet()',50);\" onunload=\"top.closeTreeWin();\"") %>

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
//-->
</script>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>