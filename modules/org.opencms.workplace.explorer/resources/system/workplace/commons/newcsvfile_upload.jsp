<%@ page import="org.opencms.workplace.explorer.*" %><%	

	// initialize the workplace class
	CmsNewCsvFile wp = new CmsNewCsvFile(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsNewResourceUpload.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;

case CmsNewResourceUpload.ACTION_OK:
//////////////////// ACTION: ok button pressed
	wp.actionSelect();
break;

case CmsNewResourceUpload.ACTION_SUBMITFORM2:
//////////////////// ACTION: upload name specified and form submitted
	wp.actionUpdateFile();
	wp.actionEditProperties(); // redirects only if the edit properties option was checked
break;

case CmsNewResourceUpload.ACTION_SUBMITFORM:
//////////////////// ACTION: upload name specified and form submitted
	wp.actionUpload();
	if (wp.getAction() == CmsNewResourceUpload.ACTION_SHOWERROR) {
		// in case of an upload error, interrupt here
		break;
	}

case CmsNewResourceUpload.ACTION_NEWFORM2:
//////////////////// ACTION: show the form to specify the resource name and the edit properties checkbox
	
	wp.setParamAction(wp.DIALOG_SUBMITFORM2);

%><%= wp.htmlStart() %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key(Messages.GUI_BUTTON_ENDWIZARD_0) %>";
	var labelNext = "<%= wp.key(Messages.GUI_BUTTON_CONTINUE_0) %>";

	function checkValue() {
		var resName = document.getElementById("newresfield").value;
		var theButton = document.getElementById("nextButton");
		if (resName.length == 0) { 
			if (theButton.disabled == false) {
				theButton.disabled =true;
			}
		} else {
			if (theButton.disabled == true) {
				theButton.disabled = false;
			}
		}
	}
	
	function toggleButtonLabel() {
		var theCheckBox = document.getElementById("newresedit");
		var theButton = document.getElementById("nextButton");
		if (theCheckBox.checked == true) {
			theButton.value = labelNext;
		} else {
			theButton.value = labelFinish;
		}
	}


//-->
</script>
<%= wp.bodyStart("dialog","onload=\"checkValue();\"") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= wp.PARAM_TITLE %>" value="<%= wp.getParamTitle() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= wp.PARAM_DIALOGTYPE %>" value="<%= wp.getParamDialogtype() %>">
<input type="hidden" name="<%= wp.PARAM_CLOSELINK %>" value="<%= wp.getParamCloseLink() %>">
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="<%= wp.PARAM_NEWRESOURCETYPE %>" value="plain">  <!-- uploaded xml content is always plain text -->

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RESOURCE_NAME_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_NEWRESOURCENAME %>" id="newresfield" type="text" value="<%= wp.getParamNewResourceName() %>" class="maxwidth" onkeyup="checkValue();" onchange="checkValue();" onpaste="setTimeout(checkValue,4);"></td>
</tr> 
<% 
if (wp.getParamCloseLink() != null) { %>
<input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" style="display: none;">
<% } else { %> 
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" class="maxwidth"><input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" checked="checked" onclick="toggleButtonLabel();">&nbsp;<%= wp.key(Messages.GUI_NEWFILE_EDITPROPERTIES_0) %></td>    
</tr>
<% } %>
</table>

<%= wp.dialogSpacer() %>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsNextCancel("id=\"nextButton\"", null) %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;

case CmsNewResourceUpload.ACTION_APPLET:
case CmsNewResourceUpload.ACTION_NEWFORM:
case CmsNewResourceUpload.ACTION_DEFAULT:
default:
//////////////////// ACTION: show the form to specify the upload file and the unzip option
	
	wp.setParamAction(wp.DIALOG_SUBMITFORM);
        wp.setParamTitle(wp.key(Messages.GUI_NEWRESOURCE_TABLE_0));

%><%= wp.htmlStart() %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key(Messages.GUI_BUTTON_ENDWIZARD_0) %>";
	var labelNext = "<%= wp.key(Messages.GUI_BUTTON_CONTINUE_0) %>";

	function checkValue() {
		var resName = document.getElementById("newresfield").value;
		var pastedData = document.getElementById("csvcontent").value;
		var theButton = document.getElementById("nextButton");
		if (resName.length == 0 && pastedData.length == 0) { 
			if (theButton.disabled == false) {
				theButton.disabled =true;
			}
		} else {
			if (theButton.disabled == true) {
				theButton.disabled = false;
			}
		}
	}
	
	function toggleButtonLabel() {
		var theCheckBox = document.getElementById("unzipfile");
		var theButton = document.getElementById("nextButton");
		if (theCheckBox.checked == true) {
			theButton.value = labelFinish;
		} else {
			theButton.value = labelNext;
		}
	}
	
	function startTimeOut() {
		// this is required for Mozilla since the onChange event dosent work there for <input type="file">
		window.setTimeout("checkValue();startTimeOut();", 500);
	}
	
	startTimeOut();	
//-->
</script>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');" enctype="multipart/form-data">
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogBlockStart(wp.key(Messages.GUI_NEWRESOURCE_SELECT_FILE_0)) %>

<table border="0" class="maxwidth">

<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_NEWRESOURCE_FILE_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_UPLOADFILE %>" id="newresfield" type="file" value="" size="60" class="maxwidth" onchange="checkValue();" onchange="checkValue();" onpaste="setTimeout(checkValue,4);"></td>
</tr> 

</table>

<%= wp.dialogBlockEnd() %>

<%= wp.dialogSpacer() %>

<%= wp.dialogBlockStart(wp.key(Messages.GUI_NEWRESOURCE_PASTE_CSV_0)) %>

<table border="0" class="maxwidth">

<tr>
<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_NEWRESOURCE_PASTE_DATA_0) %></td>

<td class="maxwidth">
<textarea id="csvcontent" class="maxwidth" name="<%= wp.PARAM_CSVCONTENT %>" cols="48" rows="7" onselect="checkValue();" onchange="checkValue();"><% 
	if (wp.getParamCsvContent() != null) {
		out.print(wp.getParamCsvContent());
	} 
%></textarea>
</td>
</tr>

</table>

<%= wp.dialogBlockEnd() %>

<%= wp.dialogSpacer() %>

<%= wp.dialogBlockStart(wp.key(Messages.GUI_NEWRESOURCE_CONVERSION_SETTINGS_0)) %>

<table border="0" class="maxwidth">

<%= wp.buildXsltSelect() %>

<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_NEWRESOURCE_CONVERSION_DELIMITER_0) %></td>
	<td class="maxwidth">
          <%= wp.buildDelimiterSelect() %>
        </td>
</tr>

</table>

<%= wp.dialogBlockEnd() %>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsNextCancel("id=\"nextButton\" disabled=\"disabled\"", null) %>
</form>
<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>