<%@ page import="
	org.opencms.workplace.*, org.opencms.report.I_CmsReport"%>
<%	
    // get workplace class from request attribute
    CmsReport wp = CmsReport.initCmsReport(pageContext, request, response);
    
    String borderStyle = "";
    String borderSimpleStyle = "";
    if (! wp.useNewStyle()) {
    	borderStyle = "2px inset ThreeDHighlight";
    	borderSimpleStyle = "2px solid ThreeDFace";
    } else {
    	borderStyle = "1px solid ThreeDShadow";
    	borderSimpleStyle = borderStyle;
    }


//////////////////// start of switch statement 
switch (wp.getAction()) {

//////////////////// ACTION: get report update
    case CmsReport.ACTION_REPORT_UPDATE:
%>
    <%= wp.htmlStart(false) %>

<script language='JavaScript'>
<!--

function a(message) {
	parent.append(message);
}

function aH(message) {
	parent.appendHead(message);
}

function aW(message) {
	parent.appendWarning(message);
}

function aE(message) {
	parent.appendError(message);
}

function aN(message) {
	parent.appendNote(message);
}

function aO(message) {
	parent.appendOk(message);
}

function aT(message) {
	parent.appendThrowable(message);
}

function aB() {
	parent.appendBr();
}

<%
String str = new String(wp.getReportUpdate());
// System.err.println(str);
%>

var active = null;

function init() {
	if (active != null) {
		clearTimeout(active);
	}
	
    var alive=<%= wp.isAlive() %>;
    
    parent.flushArray();

<%= str %>
	
    parent.update();
    
	if (alive) {
    	active = setTimeout("reload('<%= wp.REPORT_UPDATE %>');", <%= wp.REPORT_UPDATE_TIME %>);
   	} else {
   		var hasNext = "<%= wp.getParamThreadHasNext() %>";
   		if (hasNext == "true") {
   			if (!<%= wp.hasError() %> || parent.isContinueChecked()) {
   				// all actions ok or continue checked, continue automatically
   				continueReport();
   			} else {
   				// wait for user interaction
   				setTimeout('parent.stop();', 10);
   			}
   		} else {
   			setTimeout('parent.stop();', 10);
   		}
   	}
}

function reload(actionParam) {
	var resName = "<%= wp.getParamResource() %>";
	if (resName != "") {
		resName = "&resource=" + encodeURIComponent(resName);
	}
	location.href="<%= wp.getDialogRealUri() %>?<%= wp.PARAM_STYLE%>=<%=wp.getParamStyle()%>&<%= wp.PARAM_ACTION %>=" + actionParam + "&<%= wp.PARAM_THREAD %>=<%= wp.getParamThread() %>&<%= wp.PARAM_THREAD_HASNEXT %>=<%= wp.getParamThreadHasNext() %>&<%= wp.PARAM_REPORT_TYPE %>=<%= wp.getParamReportType() %>" + resName;
}



function continueReport() {
	if (!parent.buttonsInitialized) {
		parent.initButtons();
	}
	parent.disableButtons("buttonrowok");
	parent.displayButtonRowOk();
	parent.hasError = false;
	parent.lastError = "";	
	setTimeout("reload('<%= wp.REPORT_END %>');", <%= wp.REPORT_UPDATE_TIME %>);
	if (parent.document.main.<%= wp.PARAM_THREAD_HASNEXT %>) {
		parent.document.main.<%= wp.PARAM_THREAD_HASNEXT %>.value = "false";
	}
}

// -->
</script>

    <%= wp.bodyStart("empty", "style=\"background-color:ThreeDFace;\" onLoad=\"init();\"") %><%= wp.bodyEnd() %>
    <%= wp.htmlEnd() %>

<%

break;
//////////////////// ACTION: report begin
case CmsReport.ACTION_REPORT_BEGIN:
default:

wp.setParamAction(CmsReport.REPORT_END);

%>

     <%= wp.htmlStart() %>

<script type="text/javascript" language="JavaScript">
<!--

// saves the HTML of the extended report format, 
// built from the server-side generated JavaScripts
var htmlText = "";

// boolean flag whether this report is still running
var isRunning = false;

// boolean flag whether this report received the output of a warning/error message
var hasError = false;

// saves the last received headline in the report output
var lastHeadline = "";

// saves the last received warning/error message in the report output
var lastError = "";

// array to save the formats of the last received messages
var reportOutputFormats = new Array();

// array to save the last received messages
var reportOutputMessages = new Array();

// format flags for the HTML formatting of the messages
var FORMAT_DEFAULT = 0;
var FORMAT_WARNING = 1;
var FORMAT_HEADLINE = 2;
var FORMAT_NOTE = 3;
var FORMAT_OK = 4;
var FORMAT_NEWLINE = 5;
var FORMAT_THROWABLE = 6;
var FORMAT_ERROR = 7;

// saves the type of this report, which is either {simple|extended}
var reportType = "<%= wp.getParamReportType() %>";

// flag indicating that the button rows are initialized
var buttonsInitialized = false;

function append(message) {
	reportOutputFormats.push(FORMAT_DEFAULT);
	reportOutputMessages.push(message);
}

function appendHead(message) {
	reportOutputFormats.push(FORMAT_HEADLINE);
	reportOutputMessages.push(message);
	lastHeadline = "" + message;
}

function appendWarning(message) {
	reportOutputFormats.push(FORMAT_WARNING);
	reportOutputMessages.push(message);
}

function appendError(message) {
	reportOutputFormats.push(FORMAT_ERROR);
	reportOutputMessages.push(message);
	hasError = true;
	lastError = message;
}

function appendNote(message) {
	reportOutputFormats.push(FORMAT_NOTE);
	reportOutputMessages.push(message);
}

function appendOk(message) {
	reportOutputFormats.push(FORMAT_OK);
	reportOutputMessages.push(message);
}

function appendThrowable(message) {
	reportOutputFormats.push(FORMAT_THROWABLE);
	reportOutputMessages.push(message);
}

function appendBr() {
	reportOutputFormats.push(FORMAT_NEWLINE);
	reportOutputMessages.push("");
}

var report_running = new Image();
report_running.src = "<%= wp.getSkinUri() %>commons/wait.gif";

var report_ok = new Image();
report_ok.src = "<%= wp.getSkinUri() %>commons/ok.png";

var report_error = new Image();
report_error.src = "<%= wp.getSkinUri() %>commons/error.png";

// toggles between the simple and extended output format
function switchOutputFormat() {
	reportType = (reportType == "<%= I_CmsReport.REPORT_TYPE_EXTENDED %>") ? "<%= I_CmsReport.REPORT_TYPE_SIMPLE %>" : "<%= I_CmsReport.REPORT_TYPE_EXTENDED %>";

	updateReport();	
	updateReportImage();
}

var cssStyle =
    "<style type='text/css'>\n" +
    "body       { box-sizing: border-box; -moz-box-sizing: border-box; padding: 2px; margin: 0; color: #000000; background-color:#ffffff; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; }\n" +
    "div.main   { box-sizing: border-box; -moz-box-sizing: border-box; color: #000000; white-space: nowrap; }\n" +
    "span.head  { color: #000099; font-weight: bold; }\n" +
    "span.note  { color: #666666; }\n" +
    "span.ok    { color: #009900; }\n" +
    "span.warn  { color: #990000; padding-left: 40px; }\n" + 
    "span.err   { color: #990000; font-weight: bold; padding-left: 40px; }\n" +   
    "span.throw { color: #990000; font-weight: bold; }\n" +
    "span.link1 { color: #666666; }\n" +
    "span.link2 { color: #666666; padding-left: 40px; }\n" +    
    "span.link2 { color: #990000; }\n" +    
    "</style>\n";

var pageStartSimple =
    "<html>\n<head>\n" +
    "<meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=<%= wp.getEncoding() %>'>\n" + 
    "<link rel='stylesheet' type='text/css' href='<%= wp.getStyleUri("workplace.css") %>'>\n" +
    cssStyle +
    "</head>\n" +
    "<body style='background-color:Menu;'>\n" +   
    "<div style='vertical-align:middle; height: 100%;'>\n"+
    "<table border='0' style='vertical-align:middle; height: 100%;'>\n" + 
    "<tr><td width='40' align='center' valign='middle'><img name='report_img' src='<%= wp.getSkinUri() %>commons/wait.gif' width='32' height='32' alt=''></td>\n" + 
    "<td valign='middle'>";
    
var pageEndSimple = 
	"</td></tr>\n" +
	"</table></div>\n" +
	"</body>\n</html>";    

var pageStartExtended =
    "<html>\n<head>\n" +
    "<meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=<%= wp.getEncoding() %>'>\n"+ 
	cssStyle +
    "</head>\n" +
    "<body style='overflow: auto;'>\n" +  
    "<div class='main'>\n";
    
var pageEndExtended = 
	"</div>\n" +
	"</body>\n" +
	"</html>\n";                                

function start() {
	isRunning = true;
	
	initButtons();
	
	updateReportImage();
}

function stop() {
	isRunning = false;
	
	if (document.main.<%= wp.PARAM_THREAD_HASNEXT %>) {
		document.main.<%= wp.PARAM_THREAD_HASNEXT %>.value = "false";
	}
	enableButtons("buttonrowcontinue");
	enableButtons("buttonrowok");
	
	updateReport();	
	updateReportImage();
}

// sets the report image correct depending on the current state of the report
function updateReportImage() {
	if (reportType != "simple") {
		// return if the report is in not in "simple" mode currently
		return;
	}
	
	if (isRunning) {
		report.document.images["report_img"].src = report_running.src;
	} else {
		if (hasError) {
			report.document.images["report_img"].src = report_error.src;
		} else {
			report.document.images["report_img"].src = report_ok.src;
		}	
	}	
}

// flush the arrays with the report formats and messages
function flushArray() {	
    reportOutputFormats = new Array();
	reportOutputMessages = new Array();	
}

// updates the report, builds the HTML string from the JavaScript input
function update() {
	var size = <%= wp.REPORT_UPDATE_SIZE %>; 

	// resize the HTML string
    if (htmlText.length > size) {
        htmlText = htmlText.substring(htmlText.length - size, htmlText.length);
        var pos = htmlText.indexOf("\n"); 
        if (pos > 0) {
            // cut output at the first linebreak to have a "nice" start
            htmlText = htmlText.substring(pos, htmlText.length);      
        }
    }    	
	
	// append the HTML of the extended report format to the HTML string
    htmlText += getContentExtended();
		
	// write the HTML output to the iframe
	updateReport();
}

// writes the HTML output to the iframe
// this function gets also invoked when the report output format is toggled
function updateReport() {
    if (reportType == "<%= I_CmsReport.REPORT_TYPE_SIMPLE %>") {
    	if (!isRunning && hasError) {
    		pageBody = 
    			pageStartSimple + 
    			"<span class='head'>" + lastHeadline + "</span><br>\n" +
    			"<%= wp.key("report.error") %>" + lastError + "\n" +
    			"<%= wp.key("report.error.details") %>" + 
    			pageEndSimple;
    	} else {
	    	pageBody = 
	    		pageStartSimple + 
	    		"<span class='head'>" + lastHeadline + "</span><br>\n" +
	    		pageEndSimple;
    	}
    	document.getElementById("report").style.border = "<%= borderSimpleStyle %>";
    } else {
    	pageBody = pageStartExtended + htmlText + pageEndExtended;
    	document.getElementById("report").style.border = "<%= borderStyle %>";
    }

    report.document.open();    
    report.document.write(pageBody);
    report.document.close();
    
    setTimeout('doScroll();', 1);
}

// builds the HTML string from the JavaScript input
function getContentExtended() {
	var htmlStr = "";
	var i = 0;
	
	for (i=0;i<reportOutputFormats.length && i<reportOutputMessages.length;i++) {
		switch (reportOutputFormats[i]) {
			case FORMAT_WARNING :
				htmlStr += "<span class='warn'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";
				break;
			case FORMAT_ERROR :
				htmlStr += "<span class='err'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";
				break;
			case FORMAT_HEADLINE :
				htmlStr += "<span class='head'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";			
				break;
			case FORMAT_NOTE :
				htmlStr += "<span class='note'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";			
				break;
			case FORMAT_OK :
				htmlStr += "<span class='ok'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";			
				break;	
			case FORMAT_NEWLINE :
				htmlStr += "\n";
				break;	
			case FORMAT_THROWABLE :
				htmlStr += "<span class='throw'>";
				htmlStr += reportOutputMessages[i];
				htmlStr += "</span>";			
				break;
			case FORMAT_DEFAULT :			
			default :
				htmlStr += "<span>";
				htmlStr += reportOutputMessages[i];			
				htmlStr += "</span>";							
		}
	}
	
	return htmlStr;
}

function doScroll() {
	var pos = 1000000;
    report.window.scrollTo(0, pos);
}

function isContinueChecked() {
	if (document.main.continuereport && document.main.continuereport.checked == true) {
		return true;
	} else {
		return false;
	}
}

function displayButtonRowContinue() {
	displayButtonRow("buttonrowcontinue", true);
	displayButtonRow("checkcontinue", true);
	displayButtonRow("buttonrowok", false);
}

function displayButtonRowOk() {
	displayButtonRow("buttonrowcontinue", false);
	toggleButton("continuereport", true);
	displayButtonRow("buttonrowok", true);
}

function displayButtonRow(rowId, show) {
	if (show) {
		document.getElementById(rowId).className = "show";
	} else {
		document.getElementById(rowId).className = "hide";
	}
}

function enableButtons(rowId) {
	if (rowId == "buttonrowcontinue") {
		toggleButton("contok", false);
		toggleButton("contcancel", false);
	} else {
		toggleButton("okclose", false);
	}
}

function disableButtons(rowId) {
	if (rowId == "buttonrowcontinue") {
		toggleButton("contok", true);
		toggleButton("contcancel", true);
	} else {
		toggleButton("okclose", true);
		toggleButton("okcancel", true);
	}
}

function toggleButton(buttonId, disableButton) {
	if (document.getElementById(buttonId))  {
		document.getElementById(buttonId).disabled = disableButton;
	}
}

function initButtons() {
	if (document.main.<%= wp.PARAM_THREAD_HASNEXT %> && document.main.<%= wp.PARAM_THREAD_HASNEXT %>.value == "true"
			&& document.main.<%= wp.PARAM_REPORT_CONTINUEKEY %> && document.main.<%= wp.PARAM_REPORT_CONTINUEKEY %>.value != "") {
		displayButtonRowContinue();
	} else {
		displayButtonRowOk();
	}
	disableButtons("buttonrowcontinue");
	disableButtons("buttonrowok");
	buttonsInitialized = true;
}



function submitActionRefresh(para1, para2, para3) {	
 var refreshWorkplace = "<%= wp.getParamRefreshWorkplace() %>";
 if (refreshWorkplace == "true") {
     top.location.reload();
 } else {
	 return submitAction(para1, para2, para3);
 }
}

//-->
</script>

    <%= wp.bodyStart(null, "onLoad=\"start();\"") /*"dialog"*/%>
    <%= wp.dialogStart() %>

<form name="main" action="<%= wp.getDialogRealUri() %>" method="post" class="nomargin" onsubmit="return submitActionRefresh('<%= wp.DIALOG_OK %>', null, 'main');">

<%= wp.dialogContentStart(wp.getParamTitle()) %>
<%= wp.paramsAsHidden() %>

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="400">
<tr>
	<td><iframe name="report" id="report" src="about:blank" frameborder="0" style="width:99.8%; height:400px; padding: 0; margin: 0; border: <%= borderStyle %>;"></iframe></td>
</tr>
</table>

    <%= wp.dialogContentEnd() %>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr><td width="100%" id="buttonrowcontinue" class="hide">
    <%= wp.dialogButtonsContinue("id=\"contok\" onclick=\"updateWin.continueReport();\"", "id=\"contcancel\"", "id=\"contdetails\"") %>
  </td></tr>
  <tr><td width="100%" id="buttonrowok" class="hide">
    <%= wp.dialogButtonsOkCancelDetails("id=\"okclose\"", "id=\"okcancel\"", "id=\"details\"") %>
  </td></tr>
  <tr><td>
    <iframe src="<%= wp.getDialogRealUri() %>?<%= wp.PARAM_STYLE%>=<%=wp.getParamStyle()%>&<%= wp.PARAM_ACTION %>=<%= wp.REPORT_UPDATE %>&<%= 
     wp.PARAM_THREAD %>=<%= wp.getParamThread() %>&<%= wp.PARAM_REPORT_TYPE %>=<%= wp.getParamReportType() %>&<%= wp.PARAM_THREAD_HASNEXT %>=<%= 
     wp.getParamThreadHasNext() %>&resource=<%= wp.getParamResource() %>" name="updateWin" style="width:20px; height:20px; margin: 0px;" marginwidth="0" 
     marginheight="0" frameborder="0" framespacing="0" scrolling="no" class='hide'></iframe>
  </td></tr>
</table>

<table border="0" cellpadding="0" cellspacing="0" align="center"><tr>
<td class="hide" style="width: 100%; text-align: center;" id="checkcontinue"><input type="checkbox" name="continuereport" id="continuereport" value="true">&nbsp;<%= wp.key(wp.getParamReportContinueKey()) %></td>
</tr></table>

</form>

    <%= wp.dialogEnd() %>
    <%= wp.bodyEnd() %>
    <%= wp.htmlEnd() %>
<%
break;
} 
//////////////////// end of switch statement 
%>