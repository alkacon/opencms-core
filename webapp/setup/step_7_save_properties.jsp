<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%

	/* next page to be accessed */
	String nextPage = "step_8_import_workplace.jsp";

	/* true if properties are initialized */
	boolean setupOk = Bean.checkProperties();

	/* true if there are errors */
	boolean error = false;

	Vector errors = new Vector();

	if(setupOk)	{
		/* Save Properties to file "opencms.properties" */
		CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
		Utils.saveProperties(Bean.getProperties(),"opencms.properties",true);
		errors = Utils.getErrors();
		error = !errors.isEmpty();
	}

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Properties
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle; height: 100%;">

<%= Bean.getHtmlPart("C_BLOCK_START", "Saving properties ...") %>
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
	<%
		if (!error) { %>
	<tr>
		<td><img src="resources/ok.gif" border="0"></td>
		<td>&nbsp;&nbsp;</td>
		<td style="width: 100%;">Properties have been successfully saved.</td>
	</tr>
	<% 	} else	{ %>
	<tr>
		<td><img src="resources/error.gif" border="0"></td>
		<td>&nbsp;&nbsp;</td>
		<td style="width: 100%;">
			<div style="width: 100%; height:70px; overflow: auto;">
			<p style="margin-bottom: 4px;"><b>Failed:</b> Properties could not be saved!</p>
			<%
			for (int i = 0; i < errors.size(); i++)	{
				out.println(errors.elementAt(i) + "<br>");
				out.println("-------------------------------------------" + "<br>");
			}
			errors.clear();
	 		%>
			</div>
		</td>
	</tr>
	<% 	} %>		
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

<div class="dialogspacer" unselectable="on">&nbsp;</div>

<%= Bean.getHtmlPart("C_BLOCK_START", "OpenCms settings") %>
<table border="0" cellpadding="4" cellspacing="0">
	<tr>
		<td>Do you want to import the workplace?</td>
		<td>
			<input type="radio" name="importWorkplace" value="true" checked> Yes
			<input type="radio" name="importWorkplace" value="false" > No
		</td>
	</tr>
	<tr>
		<td>Enter your server's ethernet address<sup>1</sup></td>
		<td>
			<input type="text" name="ethernetAddress" value="<%= Bean.getEthernetAddress() %>" style="width: 150px;">
			
		</td>
		
	</tr>
	<tr>
		<td>Enter the name of your OpenCms server<sup>2</sup></td>		
		<td>
			<input type="text" name="serverName" value="<%= Bean.getServerName() %>" style="width: 150px;"><br>
		</td>
	</tr>
	<tr>
		<td colspan="2" style="font-size: 2px;">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="2" style="font-size: 10px;">1: You can leave this field empty, a random address will be generated.</td>
	</tr>
	<tr>
		<td colspan="2" style="font-size: 10px;">2: This name will be used for various messages.</td>
	</tr>

</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td></tr></table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(-2);">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
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