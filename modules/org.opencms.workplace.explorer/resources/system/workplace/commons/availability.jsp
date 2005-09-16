<%@ page import="org.opencms.workplace.commons.*,org.opencms.workplace.CmsDialog,org.opencms.file.CmsPropertyDefinition" %><%	

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

case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show touch dialog (default)

	wp.setParamAction("availability");
	
%><%= wp.htmlStart() %>
<script type="text/javascript">
<!--

var browseWinPresent = false;

function openDialogWin(url, name) {
	smallwindow = window.open(url, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=no,resizable=no,top=50,left=700,width=350,height=600');
	smallwindow.focus();
	browseWinPresent = true;
	return smallwindow;
}

function closeDialogWin(){
	if(browseWinPresent == true ) {
		window.smallwindow.close();
		browseWinPresent = false;
	}
}

function toggleDetail(id) {

    var element = document.getElementById(id);
    var icon = document.getElementById("ic-"+id);
    var cl = element.className;
    if (cl == "hide") {
        element.className = "show";
        icon.setAttribute("src", '<%= wp.getSkinUri() %>commons/minus.png');
    } else {
        element.className = "hide";
        icon.setAttribute("src", '<%= wp.getSkinUri() %>commons/plus.png');
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
<%= wp.calendarIncludes() %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

    <form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>
    
<%= wp.dialogBlockStart(wp.key(Messages.GUI_AVAILABILITY_0)) %>

<table border="0">

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
</table>
<%= wp.dialogBlockEnd() %>
<%= wp.dialogSpacer() %>
<%= wp.dialogBlockStart(wp.key(Messages.GUI_NOTIFICATION_SETTINGS_0)) %>
<table>
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
    <%= wp.buildCheckRecursive() %>
    <tr>
        <td colspan="3">
          <%= wp.buildResponsibleList() %>
        </td>
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
<%= wp.calendarInit(wp.PARAM_RELEASEDATE, "triggernewreleasedate", "cR", false, false, true, null, true) %>
<%= wp.calendarInit(wp.PARAM_EXPIREDATE, "triggernewexpiredate", "cR", false, false, true, null, true) %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>