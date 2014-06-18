<%@ page import="org.opencms.workplace.*" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	int buttonStyle = 0;
	
	String titleType = request.getParameter("titleType");
	String dialogTitle = wp.key(org.opencms.workplace.editors.Messages.GUI_TITLE_NEWTABLE_0);
	String disabled = "";
	if ("edit".equals(titleType)) {
		dialogTitle = wp.key(org.opencms.workplace.editors.Messages.GUI_TITLE_EDITTABLE_0);
		disabled = " disabled=\"disabled\" style=\"background-color: /*begin-color Menu*/#f0f0f0/*end-color*/;\"";
	} 
	
%><%= wp.htmlStart(null, dialogTitle) %>

<script type="text/javascript">

var ColorSelected = -1;

<!-- Object for color picker modaldialog -->
var colorPicker = new Object();
colorPicker.title = "<%= wp.key(org.opencms.workplace.editors.Messages.GUI_DIALOG_COLOR_TITLE_0)%>";
colorPicker.color = "000000";

<!-- Checks if a entered number is a digit -->

function IsDigit() {
	return ((event.keyCode >= 48) && (event.keyCode <= 57)) 
}

<!-- Sets the Table Background Color with the value returned by the Color Dialog -->
function setTBColor(arr) {
	if (ColorSelected == 1 && arr != -1 ) {
		document.main.TableColor.value = "#" + arr;	      
		window.clearInterval(CheckTBCol);
		previewColor("TableColor", "TableColor");
	}
}

function previewColor(inputId, previewId) {
	var colorValue = validateColor(document.getElementById(inputId).value);
	if (colorValue == null) {
		document.getElementById(previewId).style.color = '#000000';
		document.getElementById(previewId).style.backgroundColor = '#FFFFFF';
	} else if (colorValue < 50000) {
		document.getElementById(previewId).style.color = '#FFFFFF';	
		document.getElementById(previewId).style.backgroundColor = '#' + colorValue;			
	} else {
		document.getElementById(previewId).style.color = '#000000';
		document.getElementById(previewId).style.backgroundColor = '#' + colorValue;		
	}
}

function validateColor(string) {                // return valid color code
	string = string || '';
	string = string + "";
	string = string.toUpperCase();
	chars = '0123456789ABCDEF';
	out = '';

	for (i=0; i<string.length; i++) {             // remove invalid color chars
		schar = string.charAt(i);
		if (chars.indexOf(schar) != -1) { 
			out += schar;
		}
	}

	if (out.length != 6) { return null; }            // check length
	return out;
}

// Reads default values received by the Explorer and adds them into the Form Input-Fields when the document is loaded
function init() {
	for (elem in window.dialogArguments) {
		switch (elem) {
		case "NumRows":
			document.main.NumRows.value = window.dialogArguments["NumRows"];
			break;
		case "NumCols":
			document.main.NumCols.value = window.dialogArguments["NumCols"];
			break;
		case "Caption":
			document.main.Caption.value = window.dialogArguments["Caption"];
			break;
		case "BorderLineWidth":
			document.main.BorderLineWidth.value = window.dialogArguments["BorderLineWidth"];
			break;
		case "CellSpacing":
			document.main.CellSpacing.value = window.dialogArguments["CellSpacing"];
			break;
		case "CellPadding":
			document.main.CellPadding.value = window.dialogArguments["CellPadding"];
			break;
		case "TableAlignment":
			document.main.TableAlignment.value = window.dialogArguments["TableAlignment"];
			break;
		case "TableWidth":
			document.main.TableWidth.value = window.dialogArguments["TableWidth"];
			break;
		case "TableWidthMode":
			document.main.TableWidthMode.value = window.dialogArguments["TableWidthMode"];
			break;
		case "TableHeight":
			document.main.TableHeight.value = window.dialogArguments["TableHeight"];
			break;
		case "TableHeightMode":
			document.main.TableHeightMode.value = window.dialogArguments["TableHeightMode"];
			break;
		case "TableColor":
			document.main.TableColor.value = window.dialogArguments["TableColor"].toUpperCase();
			break;
		}
	}

	checkField("TableWidthMode", "TableWidth");
	checkField("TableHeightMode", "TableHeight");
	previewColor("TableColor", "TableColor");
}

// The "setTBColor" function is called every second to check if the color selection is completed
function openColorWindow() {
	ColorSelected=-1;
	SelColor=-1;
	CheckTBCol= window.setInterval("setTBColor(SelColor)",500);
	SelColor = showModalDialog("<%= CmsWorkplace.getSkinUri() %>components/js_colorpicker/index.html", colorPicker, "resizable: no; help: no; status: no; scroll: no;");
	if (SelColor != null) {
		ColorSelected = 1;
	} else {
		window.clearInterval(CheckTBCol);
	}
}

function checkValues() {
	var arr = new Array();

	arr["NumRows"] = document.main.NumRows.value;
	arr["NumCols"] = document.main.NumCols.value;
	arr["TableAttrs"] = " ";
	arr["CellAttrs"] = " ";
	
	if (document.main.TableAlignment.value != "") {
		arr["TableAlignment"] = document.main.TableAlignment.value;
	}

	if (document.main.BorderLineWidth.value != "") {
		arr["BorderLineWidth"] = document.main.BorderLineWidth.value;
	}

	if (document.main.CellSpacing.value != "") {
		arr["CellSpacing"] = document.main.CellSpacing.value;
	}

	if (document.main.CellPadding.value != "") {
		arr["CellPadding"] = document.main.CellPadding.value;
	}

	if (document.main.TableWidthMode.value != "") {
		arr["TableWidth"] = document.main.TableWidth.value;  
		arr["TableWidthMode"] = document.main.TableWidthMode.value;
	}

	if (document.main.TableHeightMode.value != "") {
		arr["TableHeight"] = document.main.TableHeight.value;  
		arr["TableHeightMode"] = document.main.TableHeightMode.value;  
	}

	if (document.main.Caption.value != "") {
		arr["Caption"] = document.main.Caption.value;
	} else {
		arr["Caption"] = "";  
	}

	if (document.main.TableColor.value != "") {
		arr["TableColorSelected"] ="TRUE";
		arr["TableColor"] = document.main.TableColor.value;
	} else {
		arr["TableColor"] = "";
	}

	window.returnValue = arr;
	window.close();
}

function checkField(elementId, checkFieldId) {
	var el = document.getElementById(checkFieldId);
	if (document.getElementById(elementId).value == "") {
		el.value = "";
		el.disabled = true;
		el.style.backgroundColor = "#D0D0D0";
	} else {
		el.disabled = false;
		el.style.backgroundColor = "#FFFFFF";
	}
}

function resizeWindow() {
	var wantedHeight = document.body.offsetHeight;
	if (wantedHeight > screen.availHeight) {
		wantedHeight = screen.availHeight;
	}
	window.dialogHeight = wantedHeight + "px";
	window.dialogWidth = (document.body.offsetWidth + 20) + "px";
}

</script>

<%= wp.bodyStart("dialog", "onload=\"init();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(dialogTitle) %>
<form name="main" class="nomargin" onsubmit="checkValues(); return false;">

<table border="0" cellpadding="3" cellspacing="0" width="100%">
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ROWS_0) %>:</td>
	<td><input id="NumRows" type="text" size="15" style="width: 140px;" name="NumRows" onkeypress="event.returnValue=IsDigit();"<%= disabled %>></td>
	<td class="maxwidth">&nbsp;</td>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_WIDTH_0)%>:</td>
	<td><select name="TableWidthMode" id="TableWidthMode" size="1" style="width: 150px;" onchange="checkField('TableWidthMode', 'TableWidth');"<%= disabled %>>
  			<option value="" selected="selected"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_TABLE_NOTSPECIFIED_0)%></option>
      		<option value="%"> <%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_INPERCENT_0) %>
      		<option value="pixel"> <%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_INPIXEL_0) %>
      	</select></td>
    <td><input id="TableWidth" type="text" size="5" name="TableWidth" onkeypress="event.returnValue=IsDigit();"></td>
</tr>
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_COLUMNS_0)%>:</td>
	<td><input id="NumCols" type="text" size="15" style="width: 140px;" name="NumCols" onkeypress="event.returnValue=IsDigit();"<%= disabled %>></td>
	<td class="maxwidth">&nbsp;</td>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_HEIGHT_0) %>:</td>
	<td><select name="TableHeightMode" id="TableHeightMode" size="1" style="width: 150px;" onchange="checkField('TableHeightMode', 'TableHeight');"<%= disabled %>>
  			<option value="" selected="selected"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_TABLE_NOTSPECIFIED_0) %></option>
      		<option value="%"> <%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_INPERCENT_0)%>
      		<option value="pixel"> <%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_INPIXEL_0) %>
      	</select></td> 
    <td><input id="TableHeight" type="text" size="5" name="TableHeight" onkeypress="event.returnValue=IsDigit();"></td>	
</tr>
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_TITLE_0) %>:</td>
  	<td colspan="2"><input type="text" size="15" style="width: 140px;" name="Caption"<%= disabled %>></td>
	<td class="maxwidth">&nbsp;</td>
	<td colspan="2">&nbsp;</td>  	
</tr>
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNMENT_0)%>:</td>
	<td><select id="TableAlignment" name="TableAlignment" size="1" style="width: 140px;"<%= disabled %>>
			<option value="" selected="selected"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_TABLE_NOTSPECIFIED_0) %></option>
      		<option value="left"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNLEFT_0)%></option>
      		<option value="center"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNCENTER_0) %></option>
      		<option value="right"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNRIGHT_0)%></option>
      	</select> 
    </td>
    <td class="maxwidth">&nbsp;</td>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_PADDING_0) %>: </td>
  	<td style="white-space: nowrap;" colspan="2"><input id="CellPadding" type="text" size="5" name="CellPadding" onkeypress="event.returnValue=IsDigit();">
  	<%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_PADDINGINFO_0)%></td>
</tr>
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_BORDER_0) %>:</td>
	<td><input id="BorderLineWitdh" type="text" size="15" style="width: 140px;" name="BorderLineWidth" onkeypress="event.returnValue=IsDigit();"></td>
    <td class="maxwidth">&nbsp;</td>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_SPACING_0) %>: </td>
  	<td style="white-space: nowrap;" colspan="2"><input id="CellSpacing" type="text" size="5" name="CellSpacing" onkeypress="event.returnValue=IsDigit();">
  	<%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_SPACINGINFO_0) %></td>
</tr>
<tr>
	<td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_COLOR_0) %></td>
	<td><input type="text" id="TableColor" name="TableColor" size="15" style="width: 140px;" maxlength="7" onkeyup="previewColor('TableColor', 'TableColor');"></td>
	<td colspan="4"><table border="0" cellpadding="0" cellspacing="0">
						<tr>
							<%= wp.button("javascript:openColorWindow();", null, "color_fill", org.opencms.workplace.editors.Messages.GUI_BUTTON_COLOR_0 , buttonStyle) %>
						</tr>
					</table>
	</td>
</tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel(null, "onclick=\"window.close();\"") %>
</form>

<%= wp.dialogEnd() %><script type="text/javascript">
<!--
setTimeout("resizeWindow()", 100);
//-->
</script>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
