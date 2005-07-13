<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsTouch wp = new CmsTouch(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsTouch.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsTouch.ACTION_TOUCH:	
case CmsTouch.ACTION_WAIT:

//////////////////// ACTION: main touching action (with optional wait screen)

	wp.actionTouch();

break;

case CmsTouch.ACTION_DEFAULT:
default:

//////////////////// ACTION: show touch dialog (default)

	wp.setParamAction("touch");
	
%><%= wp.htmlStart() %>
<%= wp.calendarIncludes() %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>

<form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_TOUCH_NEW_TIMESTAMP_0) %>
	<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= wp.PARAM_NEWTIMESTAMP %>" id="<%= wp.PARAM_NEWTIMESTAMP %>" value="<%= wp.getCurrentDateTime() %>"></td>
	<td>&nbsp;<img src="<%= wp.getSkinUri() %>buttons/calendar.png" id="triggercalendar" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_RELEASED_0) %>
	<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= wp.PARAM_RELEASEDATE %>" id="<%= wp.PARAM_RELEASEDATE %>" value="<%= wp.getCurrentReleaseDate() %>"></td>
	<td>&nbsp;<img src="<%= wp.getSkinUri() %>buttons/calendar.png" id="triggernewreleasedate" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_LABEL_DATE_EXPIRED_0) %>
	<td style="width: 300px;"><input class="maxwidth" type="text" name="<%= wp.PARAM_EXPIREDATE %>" id="<%= wp.PARAM_EXPIREDATE %>" value="<%= wp.getCurrentExpireDate() %>"></td>
	<td>&nbsp;<img src="<%= wp.getSkinUri() %>buttons/calendar.png" id="triggernewexpiredate" alt="<%= wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" title="<%=  wp.key(Messages.GUI_CALENDAR_CHOOSE_DATE_0) %>" border="0"></td>
</tr>
<%= wp.buildCheckRecursive() %>
</table>

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

%><%= wp.calendarInit(wp.PARAM_NEWTIMESTAMP, "triggercalendar", "cR", false, false, true, null, true) %>
<%= wp.calendarInit(wp.PARAM_RELEASEDATE, "triggernewreleasedate", "cR", false, false, true, null, true) %>
<%= wp.calendarInit(wp.PARAM_EXPIREDATE, "triggernewexpiredate", "cR", false, false, true, null, true) %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>