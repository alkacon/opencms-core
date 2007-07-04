<%@ page import="
    org.opencms.workplace.commons.*,
    org.opencms.workplace.CmsDialog,
    org.opencms.widgets.CmsCalendarWidget    
"%><%	

	// initialize the workplace class
	CmsAvailability wp = new CmsAvailability(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsDialog.ACTION_OK:	
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main touching action (with optional wait screen)

	wp.actionUpdate();

break;

case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show touch dialog (default)

	wp.setParamAction("availability");
	
%><%= wp.htmlStart() %>
<%@page import="org.opencms.workplace.CmsWorkplace"%>
<script type="text/javascript">
<!--

var oldReleaseValue;
setTimeout("checkReleaseFields()", 500);

function checkReleaseFields(fieldId) {
	
	var resetRel = document.getElementById("<%= CmsAvailability.PARAM_RESETRELEASE %>");
	var dateRel = document.getElementById("<%= CmsAvailability.PARAM_RELEASEDATE %>");
	
	if (fieldId == "<%= CmsAvailability.PARAM_RESETRELEASE %>") {
		if (resetRel.checked) {
			dateRel.value = "";
		} else {
			dateRel.value = document.forms["main"].elements["hiddenrelease"].value;
		}
	} else {
		var newDateValue = dateRel.value;
		if ((newDateValue != oldReleaseValue) && newDateValue != "<%= CmsTouch.DEFAULT_DATE_STRING %>") {
			resetRel.checked = false;
		} 
	}
	oldReleaseValue = dateRel.value;
	setTimeout("checkReleaseFields()", 500);
}

var oldExpireValue;
setTimeout("checkExpireFields()", 500);

function checkExpireFields(fieldId) {
	
	var resetExp = document.getElementById("<%= CmsAvailability.PARAM_RESETEXPIRE %>");
	var dateExp = document.getElementById("<%= CmsAvailability.PARAM_EXPIREDATE %>");
	
	if (fieldId == "<%= CmsAvailability.PARAM_RESETEXPIRE %>") {
		if (resetExp.checked) {
			dateExp.value = "";
		} else {
			dateExp.value = document.forms["main"].elements["hiddenexpire"].value;
		}
	} else {
		var newDateValue = dateExp.value;
		if ((newDateValue != oldExpireValue) && newDateValue != "<%= CmsTouch.DEFAULT_DATE_STRING %>") {
			resetExp.checked = false;
		} 
	}
	oldExpireValue = dateExp.value;
	setTimeout("checkExpireFields()", 500);
}

function toggleDetail(id) {

    var element = document.getElementById(id);
    var icon = document.getElementById("ic-"+id);
    var cl = element.className;
    if (cl == "hide") {
        element.className = "show";
        icon.setAttribute("src", '<%= CmsWorkplace.getSkinUri() %>commons/minus.png');
    } else {
        element.className = "hide";
        icon.setAttribute("src", '<%= CmsWorkplace.getSkinUri() %>commons/plus.png');
    }
}

function toggleInheritInfo() {

    var button = document.getElementById("button");
    var element;
    for(var i=0;true;i++) {
        var id = "inheritinfo" + i;
        element = document.getElementById(id);
        if (element == null) {
            break;
        }
        var cl = element.className;
        if (cl == "hide") {
            element.className = "show";
            button.setAttribute("value", "Zusammenfassung");
        } else {
            element.className = "hide";
            button.setAttribute("value", "Details");
        }
    }
}

//-->
</script>
<%= CmsCalendarWidget.calendarIncludes(wp.getLocale()) %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

    <form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="hiddenrelease" value="<%= wp.getCurrentReleaseDate() %>">
<input type="hidden" name="hiddenexpire" value="<%= wp.getCurrentExpireDate() %>">

<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { //%>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { //%>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>
<%= wp.dialogSpacer() %>

<%= wp.buildCheckRecursive() %>

<%= wp.dialogBlockStart(wp.key(Messages.GUI_AVAILABILITY_0)) %>
<table border="0">
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_RELEASED_0) %>
		<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= CmsAvailability.PARAM_RELEASEDATE %>" id="<%= CmsAvailability.PARAM_RELEASEDATE %>" value="<%= wp.getCurrentReleaseDate() %>"></td>
		<td>&nbsp;<img src="<%= CmsWorkplace.getSkinUri() %>buttons/calendar.png" id="triggernewreleasedate" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
    </tr>
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_AVAILABILITY_RESET_RELEASE_0) %>
		<td colspan="2"><input type="checkbox" name="<%= CmsAvailability.PARAM_RESETRELEASE %>" id="<%= CmsAvailability.PARAM_RESETRELEASE %>" value="true" onclick="checkReleaseFields('<%= CmsAvailability.PARAM_RESETRELEASE %>');"></td>
    </tr>
    
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_EXPIRED_0) %>
		<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= CmsAvailability.PARAM_EXPIREDATE %>" id="<%= CmsAvailability.PARAM_EXPIREDATE %>" value="<%= wp.getCurrentExpireDate() %>"></td>
		<td>&nbsp;<img src="<%= CmsWorkplace.getSkinUri() %>buttons/calendar.png" id="triggernewexpiredate" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
    </tr>
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_AVAILABILITY_RESET_EXPIRE_0) %>
		<td colspan="2"><input type="checkbox" name="<%= CmsAvailability.PARAM_RESETEXPIRE %>" id="<%= CmsAvailability.PARAM_RESETEXPIRE %>" value="true" onclick="checkExpireFields('<%= CmsAvailability.PARAM_RESETEXPIRE %>');"></td>
    </tr>
</table>
<%= wp.dialogBlockEnd() %>

<%= wp.dialogSpacer() %>

<%= wp.dialogBlockStart(wp.key(Messages.GUI_NOTIFICATION_SETTINGS_0)) %>
<table border="0">
    <tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_NOTIFICATION_INTERVAL_0) %>
	<td style="width: 300px;">
          <%= wp.buildInputNotificationInterval() %>
        </td>
	<td>&nbsp</td>
    </tr>
    <tr>
        <td style="white-space:nowrap;"><%= wp.key(Messages.GUI_AVAILABILITY_ENABLE_NOTIFICATION_0) %></td>
        <td class="maxwidth" style="padding-left: 5px;">
          <%= wp.buildCheckboxEnableNotification() %>
        </td>
        <td>&nbsp</td>
    </tr>
    <%= wp.buildCheckboxModifySiblings() %>
    <%= wp.buildResponsibleList() %>
</table>
<%= wp.dialogBlockEnd() %>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>
</form>
<%= wp.dialogEnd() %>
<%
    /**
     * This initializes the JS calendar.<p>
     * 
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param disableFunc JS function which determines if a date should be disabled or not
     * @param showTime true if the time selector should be shown, otherwise false
     */

%>
<%= CmsCalendarWidget.calendarInit(wp.getMessages(), CmsAvailability.PARAM_RELEASEDATE, "triggernewreleasedate", "cR", false, false, true, null, true) %>
<%= CmsCalendarWidget.calendarInit(wp.getMessages(), CmsAvailability.PARAM_EXPIREDATE, "triggernewexpiredate", "cR", false, false, true, null, true) %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<% 
   break;

case CmsDialog.ACTION_DEFAULT:
default:
    %>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>