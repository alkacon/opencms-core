<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />


<%
    /* true if properties are initialized */
    boolean setupOk = (Bean.getProperties()!=null);

    /* next page to be accessed */
    String nextPage = "step_8_browser_configuration_notes.jsp";


%>

<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
	<script type="text/javascript">

		var enabled = false;
		var finished = false;
		var animation;
		var message = "Importing workplace ... please wait";
		var countchar = 0;

		/* indicates if the document has been loaded */
		function enable() {
			enabled = true;
			parent.data.location.href="step_7b_data_import.jsp";
			document.forms[0].info.value = message;
		}

		/* displays the given output */
		function start(out) {
			if(enabled) {
				document.forms[0].ctn.disabled = true;
				document.forms[0].bck.disabled = true;
				temp ="";
				for(var i=out.length-1;i>=0;i--)    {
					temp += unescape(out[i])+"\n";
				}
				var oldcontent = document.forms[0].output.value;
				document.forms[0].output.value = temp + oldcontent;
			}
		}

		/* Displays a message and enables the continue button */
		function finish() {
			document.forms[0].info.value = "Finished. Please check the output below to ensure that the workplace has been imported without major errors.";
			document.forms[0].ctn.disabled = false;
			document.forms[0].bck.disabled = false;
			finished = true;
		}

		/* if finished, you can access next page */
		function nextpage() {
			if(finished) {
				top.location.href="<%= nextPage %>";
			}
		}

		/* if finished, you can go back */
		function lastpage() {
			if(finished)    {
				history.back(-2);
			}
		}
	</script>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Import workplace
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>

<% if(setupOk) { %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
	<b>Status:</b><br><input type="text" style="width:650px" size="60" name="info">
	</td>
</tr>
<tr>
	<td align="center" valign="top">
			<textarea style="width:650px;height:325px;" cols="60" rows="16" wrap="off" name="output" id="output"></textarea>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="bck" id="bck" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="lastpage();">
<input name="ctn" id="ctn" type="button" value="Continue &#062;&#062;" class="dialogbutton" onclick="nextpage();">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<script type="text/javascript">
	enable();
</script>
<% } else	{ %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<p><b>ERROR</b></p>
		The setup wizard has not been started correctly!<br>
		Please click <a href="">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
