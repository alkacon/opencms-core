<%@ page import="org.opencms.util.*,org.opencms.workplace.editors.*" %><%	

	// initialize the workplace class
	CmsDialogElements wp = new CmsDialogElements(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialogElements.ACTION_UPDATE_ELEMENTS:

//////////////////// ACTION: update the enabled/disabled elements

	wp.actionUpdateElements();
%>

	<html><head>
	<script type="text/javascript">
		function closeAction() {
			this.elemName = "<%= wp.getChangeElement() %>";
			this.elemLocale = "<%= wp.getElementLocale() %>";
		}
		var closeObj = new closeAction();
		if (window.opener.popupCloseAction) {
			window.opener.popupCloseAction(closeObj);
		}
		window.close();
	</script>
	</head></html>

<%
break;

case CmsDialogElements.ACTION_DEFAULT:
default:

//////////////////// ACTION: show initial template dialog (default)

	wp.setParamAction(wp.DIALOG_UPDATE_ELEMENTS);

%><%= wp.htmlStart(null, wp.getParamTitle()) %>
<script type="text/javascript">
<!--

function confirmDelete() {
	var isDeleted = false;
	for (var i=0; i<elems.length; i++) {
		var elemName = "<%= wp.PREFIX_PARAM_BODY %>" + elems[i]["name"];
		if (elems[i]["enabled"] && (document.forms["main"].elements[elemName].checked == false)) {
			isDeleted = true;
		}
	}
	if (isDeleted) {
		var confirmText = "<%= CmsStringUtil.escapeJavaScript(wp.key("editor.dialog.elements.confirmdisable")) %>";
		if (confirm(confirmText)) {	
			document.main.submit();
		}
	} else {
		document.main.submit();
	}
}

function resizeWindow() {
	var wantedHeight = document.body.offsetHeight + 20;
	if (wantedHeight > screen.availHeight) {
		wantedHeight = screen.availHeight;
	}
	window.resizeTo(document.body.offsetWidth + 10, wantedHeight);
}

var elems = new Array();

function registerElement(elemName, isEnabled) {
	var i = elems.length; 
	elems[i] = new Object();
	elems[i]["name"] = elemName;
	elems[i]["enabled"] = isEnabled;
}

//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.buildElementList() %>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOk(" onclick=\"confirmDelete();\"") %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%
if ("true".equals(wp.getParamIsPopup())) {
	%><script type="text/javascript">
<!--
resizeWindow();
//-->
</script>
<%
} 
%>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>