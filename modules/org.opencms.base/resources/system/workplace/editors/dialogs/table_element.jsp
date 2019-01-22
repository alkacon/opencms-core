<%@ page import="org.opencms.workplace.*" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	int buttonStyle = 0;
	
	String titleType = request.getParameter("titleType");
	String dialogTitle = wp.key(org.opencms.workplace.editors.Messages.GUI_TITLE_CHANGETD_0);
	if ("TR".equals(titleType)) {
		dialogTitle = wp.key(org.opencms.workplace.editors.Messages.GUI_TITLE_CHANGETR_0);
	} 
	
%><%= wp.htmlStart(null, dialogTitle) %>

<script type="text/javascript">

function resizeWindow() {
	var wantedHeight = document.body.offsetHeight;
	if (wantedHeight > screen.availHeight) {
		wantedHeight = screen.availHeight;
	}
	window.dialogHeight = wantedHeight + "px";
	window.dialogWidth = (document.body.offsetWidth + 20) + "px";
}

var ColorSelected = -1;
var colorField = null;

<!-- Object for color picker modaldialog -->
var colorPicker = new Object();
colorPicker.title = "<%=  wp.key(org.opencms.workplace.editors.Messages.GUI_DIALOG_COLOR_TITLE_0) %>";
colorPicker.color = "000000";

<!-- Checks if a entered number is a digit -->

function IsDigit() {
	return ((event.keyCode >= 48) && (event.keyCode <= 57))
}

// Reads default values received by the Explorer and adds them into the Form Input-Fields when the document is loaded
function init() {
	for ( elem in window.dialogArguments ) {
		switch( elem ) {
		case "bgColor":
			document.main.TBGColor.value = window.dialogArguments["bgColor"].toUpperCase();
			break;
		case "borderColor":
			document.main.TBorderColor.value = window.dialogArguments["borderColor"].toUpperCase();
			break;
		case "height":
			document.main.TDHeight.value = window.dialogArguments["height"];
			break;
		case "width":
			document.main.TDWidth.value = window.dialogArguments["width"];
			break;
		case "align":
			if(window.dialogArguments["align"] == "left") {
				document.main.TDAlign.selectedIndex = 1;
			} else if(window.dialogArguments["align"] == "center") {
				document.main.TDAlign.selectedIndex = 2;
			} else if(window.dialogArguments["align"] == "right") {
				document.main.TDAlign.selectedIndex = 3;
			} else {
				document.main.TDAlign.selectedIndex = 0;
			}
			break;
		case "vAlign":
			if(window.dialogArguments["vAlign"] == "top") {
				document.main.TDVAlign.selectedIndex = 1;
			} else if(window.dialogArguments["vAlign"] == "middle") {
				document.main.TDVAlign.selectedIndex = 2;
			} else if(window.dialogArguments["vAlign"] == "bottom") {
				document.main.TDVAlign.selectedIndex = 3;
			} else if(window.dialogArguments["vAlign"] == "baseline") {
				document.main.TDVAlign.selectedIndex = 4;
			} else {
				document.main.TDVAlign.selectedIndex = 0;
			}
			break;
		case "error_notable":
			if (window.dialogArguments["error_notable"] == "true") {
				// Not inside table, display message and close window
				alert("<%=  wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_NOTABLE_0) %>");
				window.close();
			}
			break;
		case "error_selection":
			if (window.dialogArguments["error_selection"] == "true") {
				// Not selection.type = "None", display message and close window
				alert("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_SELECTION_0) %>");
				window.close();
			}
			break;
		}
	}
	previewColor("TBorderColor", "TBorderColor");
	previewColor("TBGColor", "TBGColor");
}

// The "setTBColor" function is called every second to check if the color selection is completed
function openColorWindow(colorType) {
	ColorSelected=-1;
	SelColor=-1;
	colorField = colorType;
	CheckColor= window.setInterval("setColor(SelColor, colorField)", 500);
	SelColor = showModalDialog("<%= CmsWorkplace.getSkinUri() %>components/js_colorpicker/index.html", colorPicker, "resizable: no; help: no; status: no; scroll: no;");
	if (SelColor != null) {
		ColorSelected = 1;
	} else {
		window.clearInterval(CheckColor);
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
	out   = '';

	for (i=0; i<string.length; i++) {             // remove invalid color chars
		schar = string.charAt(i);
		if (chars.indexOf(schar) != -1) {
			out += schar;
		}
	}

	if (out.length != 6) {
		return null;
	}
	return out;
}

function setColor(arr, colorFieldName) {
	if (ColorSelected == 1 && arr != -1 ) {
		document.main.elements[colorFieldName].value = "#" + arr;
		window.clearInterval(CheckColor);
	}
	previewColor(colorFieldName, colorFieldName);
}

function checkValues() {
	var varAlign = document.main.TDAlign.options[document.main.TDAlign.selectedIndex].value;
	var varVAlign = document.main.TDVAlign.options[document.main.TDVAlign.selectedIndex].value;
	var arr = new Array();
	if (document.main.TDHeight.value.length > 0) {
		arr["height"] = document.main.TDHeight.value;
	} else {
		arr["height"] = null;
	}
	if (document.main.TDWidth.value.length > 0) {
		arr["width"] = document.main.TDWidth.value;
	} else {
		arr["width"] = null;
	}
	if (varAlign.length > 0) {
		arr["align"] = varAlign;
	} else {
		arr["align"] = null;
	}
	if (varVAlign.length > 0) {
		arr["vAlign"] = varVAlign;
	} else {
		arr["vAlign"] = null;
	}
	if (document.main.TBGColor.value.length > 0) {
		arr["bgColor"] = document.main.TBGColor.value;
	} else {
		arr["bgColor"] = null;
	}
	if (document.main.TBorderColor.value.length > 0) {
		arr["borderColor"] = document.main.TBorderColor.value;
	} else {
		arr["borderColor"] = null;
	}
	
	// This is the difference between Cancel and setting all attributes to null !!
	arr["Ok"] = "Ok";

	window.returnValue = arr;
	window.close();
}

function checkField(elementId, checkFieldId) {
	var el = document.getElementById(checkFieldId);
	if (document.getElementById(elementId).value == "") {
		el.value = "";
		el.disabled = true;
	} else {
		el.disabled = false;
	}
}

</script>

<%= wp.bodyStart("dialog", "onload=\"init();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(dialogTitle) %>
<form name="main" class="nomargin" onsubmit="checkValues(); return false;">

<table border="0" cellpadding="3" cellspacing="0">
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_HEIGHT_0) %>:</td>
  <td><input id="TDHeight" type="text" size="7" maxlength="7" style="width:150px;" name="TDHeight" onkeypress="event.returnValue=IsDigit();"></td>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_BORDERINFO_0)%></td>
</tr>
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_WIDTH_0)%>: </td>
  <td><input id="TDWidth" type="text" size="7" maxlength="7" style="width:150px;" name="TDWidth" onkeypress="event.returnValue=IsDigit();"></td>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_BORDERINFO_0) %></td>
</tr>  
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGN_0)%>: </td>
  <td colspan="2"><select name="TDAlign" id="TDAlign" size="1" style="width:150px;">
        <option value=""><%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_TABLE_NOTSPECIFIED_0)%></option>
        <option value="left"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNLEFT_0)%></option>
        <option value="center"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNCENTER_0) %></option>
        <option value="right"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_ALIGNRIGHT_0) %></option>
      </select>
</tr>
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_VALIGN_0)%>: </td>
  <td colspan="2"><select name="TDVAlign" id="TDVAlign" size="1" style="width:150px;">
        <option value=""><%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_TABLE_NOTSPECIFIED_0) %></option>
        <option value="top"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_VALIGNTOP_0) %></option>
        <option value="middle"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_VALIGNMIDDLE_0) %></option>
        <option value="bottom"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_VALIGNBOTTOM_0) %></option>        
        <option value="baseline"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_VALIGNBASELINE_0) %></option>
      </select>
</tr>
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_TBGCOLOR_0) %>:</td>
  <td><input type="text" id="TBGColor" size=7 maxlength="7" name="TBGColor" style="width:150px;" onkeyup="previewColor('TBGColor', 'TBGColor');"></td>
  <td><table border="0" cellpadding="0" cellspacing="0">
  		<tr>
  			<%= wp.button("javascript:openColorWindow('TBGColor');", null, "color_fill", org.opencms.workplace.editors.Messages.GUI_BUTTON_COLOR_0, buttonStyle) %>
  		</tr>
  	  </table>  
  </td>
</tr>
<tr>
  <td style="white-space: nowrap;"><%= wp.key(org.opencms.workplace.editors.Messages.GUI_INPUT_TBORDERCOLOR_0)%>:</td>
  <td><input type="text" id="TBorderColor" size=7 maxlength="7" name="TBorderColor" style="width:150px;" onkeyup="previewColor('TBorderColor', 'TBorderColor');"></td>
  <td><table border="0" cellpadding="0" cellspacing="0">
  		<tr>
  			<%= wp.button("javascript:openColorWindow('TBorderColor');", null, "color_fill", org.opencms.workplace.editors.Messages.GUI_BUTTON_COLOR_0, buttonStyle) %>
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