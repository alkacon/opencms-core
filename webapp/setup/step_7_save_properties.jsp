<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%

	/* next page to be accessed */
	String nextPage = "step_8_import_workplace.jsp";
	
	/* previous page in the setup process */
	String prevPage = "step_6_module_selection.jsp";

	/* true if properties are initialized */
	boolean setupOk = Bean.checkProperties();

	/* true if there are errors */
	boolean error = false;

	Vector errors = new Vector();

	if (!setupOk) {
		Bean.initHtmlParts();
	}

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Settings
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 350px;">
<tr>
	<td style="vertical-align: top;">

<%= Bean.getHtmlPart("C_BLOCK_START", "OpenCms settings") %>
<table border="0" cellpadding="4" cellspacing="0">
	<tr>
		<td>Enter your server's ethernet address</td>
		<td>
			<input type="text" name="ethernetAddress" value="<%= Bean.getEthernetAddress() %>" style="width: 150px;">
			
		</td>
		<td><%= Bean.getHtmlHelpIcon("2", "") %></td>
	</tr>
	<tr>
		<td>Enter the name of your OpenCms server</td>		
		<td>
			<input type="text" name="serverName" value="<%= Bean.getServerName() %>" style="width: 150px;">
		</td>
		<td><%= Bean.getHtmlHelpIcon("3", "") %></td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

<div class="dialogspacer" unselectable="on">&nbsp;</div>

<%= Bean.getHtmlPart("C_BLOCK_START", "Workplace import") %>
<table border="0" cellpadding="4" cellspacing="0">
	<tr>
		<td>Do you want to import the workplace?</td>
		<td>
			<input type="radio" name="importWorkplace" value="true" checked="checked"> Yes
			<input type="radio" name="importWorkplace" value="false" > No
		</td>
		<td><%= Bean.getHtmlHelpIcon("1", "") %></td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td></tr></table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "1") %>
<b>Importing the Workplace is required for an OpenCms installation.</b><br>&nbsp;<br>
Do not change this setting unless you know exactly what you do!<br>&nbsp;<br>
This imports all resources for the OpenCms Workplace in the virtual file system (VFS).
A scenario where you might not want to import the Workplace is e.g. 
to connect a second server against an already initialized OpenCms database.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "3") %>
<b>The server name</b><br>&nbsp;<br>
This server name will be used for various log messages in OpenCms.<br>&nbsp;<br>
This can be handy if you have to compare logfiles from a couple of different servers.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "2") %>
<b>Why the ethernet address is needed:</b><br>&nbsp;<br>
OpenCms generates unique keys for all resources based on a 
128-bit UUID (Universally Unique IDentifier, aka GUID in the Windows world) algorithm.
To initialize this algorithm, the ethernet address of the server is required.
However, Java has no way of accessing this information from the server hardware 
because of the sandbox security model.<br>&nbsp;<br>
You can leave this field empty, and a random ethernet address will be generated for your OpenCms server.
This means there is a <i>very, very, very slight</i> chance that someone else in the universe might create some duplicate keys.
<%= Bean.getHtmlPart("C_HELP_END") %>

<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>