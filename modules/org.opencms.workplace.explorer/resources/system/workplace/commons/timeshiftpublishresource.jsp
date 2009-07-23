<%@ page import="
    org.opencms.workplace.commons.*,
    org.opencms.workplace.CmsDialog,
    org.opencms.widgets.CmsCalendarWidget    
"%><%	

	// initialize the workplace class
	CmsTimeShiftPublish wp = new CmsTimeShiftPublish(pageContext, request, response);
	
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

	wp.setParamAction("timeshiftpublish");
	
%><%= wp.htmlStart() %>
<%@page import="org.opencms.workplace.CmsWorkplace"%>
<script type="text/javascript">
<!--

var oldTimeshiftPublishValue;
setTimeout("checkTimeshiftPublishFields()", 500);

function checkTimeshiftPublishFields(fieldId) {
	
	var resetRel = document.getElementById("<%= CmsTimeShiftPublish.PARAM_RESETTIMESHIFTPUBLISH %>");
	var dateRel = document.getElementById("<%= CmsTimeShiftPublish.PARAM_TIMESHIFTPUBLISHDATE %>");
	
	if (fieldId == "<%= CmsTimeShiftPublish.PARAM_RESETTIMESHIFTPUBLISH %>") {
		if (resetRel.checked) {
			dateRel.value = "";
		} else {
			dateRel.value = document.forms["main"].elements["hiddentimeshiftpublish"].value;
		}
	} else {
		var newDateValue = dateRel.value;
		if ((newDateValue != oldTimeshiftPublishValue) && newDateValue != "<%= CmsTouch.DEFAULT_DATE_STRING %>") {
			resetRel.checked = false;
		} 
	}
	oldTimeshiftPublishValue = dateRel.value;
	setTimeout("checkTimeshiftPublishFields()", 500);
}

//-->
</script>
<%= CmsCalendarWidget.calendarIncludes(wp.getLocale()) %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

    <form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="hiddentimeshiftpublish" value="">

<%= wp.dialogContentStart(wp.getParamTitle()) %>
<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>
<%= wp.dialogBlockStart(wp.key(Messages.GUI_TIMESHIFT_PUBLISH_0)) %>
<table border="0">
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_TIMESHIFT_PUBLISH_0) %>
		<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= CmsTimeShiftPublish.PARAM_TIMESHIFTPUBLISHDATE %>" id="<%= CmsTimeShiftPublish.PARAM_TIMESHIFTPUBLISHDATE %>" value=""></td>
		<td>&nbsp;<img src="<%= CmsWorkplace.getSkinUri() %>buttons/calendar.png" id="triggernewtimeshiftpublishdate" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
    </tr>
    <tr>
		<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_RESET_TIMESHIFT_PUBLISH_0) %>
		<td colspan="2"><input type="checkbox" name="<%= CmsTimeShiftPublish.PARAM_RESETTIMESHIFTPUBLISH %>" id="<%= CmsTimeShiftPublish.PARAM_RESETTIMESHIFTPUBLISH %>" value="true" onclick="checkTimeshiftPublishFields('<%= CmsTimeShiftPublish.PARAM_RESETTIMESHIFTPUBLISH %>');"></td>
    </tr>
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
<%= CmsCalendarWidget.calendarInit(wp.getMessages(), CmsTimeShiftPublish.PARAM_TIMESHIFTPUBLISHDATE, "triggernewtimeshiftpublishdate", "cR", false, false, true, null, true) %>
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