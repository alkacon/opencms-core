<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

// next page 
String nextPage = "step_2_check_components.jsp";
// previous page
String prevPage = "index.jsp";

// Reading the system properties
Properties vmProperties = System.getProperties();
String vmEncoding = vmProperties.getProperty("file.encoding");
boolean encodingOk = false;

if (Bean.isInitialized()) {
	encodingOk = Bean.getDefaultContentEncoding().equalsIgnoreCase(vmEncoding);
	if (encodingOk) {
		response.sendRedirect(nextPage);
	}
}

%><%--

--%><%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Wrong content encoding!
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized()) { %>
<table border="0" cellpadding="0" cellspacing="0" style="height: 100%;">
<tr><td style="vertical-align: bottom;">

<%= Bean.getHtmlPart("C_BLOCK_START", "Error") %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%;">
<tr>
	<td style="vertical-align: middle;" rowspan="2"><img src="resources/error.gif" width="32" height="32" border="0"></td>
	<td style="font-weight: bold;">
		The encoding of your Java VM is different from the OpenCms encoding!
	</td>
</tr>
<tr>
	<td>
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td style="text-align: left; padding-bottom: 4px;">Java VM file encoding:&nbsp;</td><td style="text-align: left; padding-bottom: 4px; font-weight: bold;"><%= vmEncoding %></td>
			</tr>
			
			<tr>
				<td style="text-align: left;">OpenCms encoding:</td><td style="text-align: left; font-weight: bold;"><%= Bean.getDefaultContentEncoding() %></td>
			</tr>
		</table>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

<div class="dialogspacer" unselectable="on">&nbsp;</div>

</td></tr>
<tr><td style="vertical-align: top;">

<%= Bean.getHtmlPart("C_BLOCK_START", "How to continue the setup process") %>
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;"><tr><td>
<div style="width: 100%; height:210px; overflow: auto;">

<p style="margin-top: 5px; font-weight: bold;">Changing the OpenCms default encoding:</p>
<p>
Unless you have specific encoding requirements, you should use the default <b>ISO-8859-1</b> setting.
Please refer to the <a href="http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html" target="_blank">Sun documentation</a>
for a list of supported encodings for your OS.
</p>
<ul>
	<li>If you want to use an encoding different from <span style="font-weight: bold;">ISO-8859-1</span>, you must
	adjust the <code>defaultContentEncoding</code> setting in the OpenCms properties<br>
	<code>WEB-INF/config/opencms.properties</code>.</li>
</ul>


<p style="font-weight: bold;">Configuring Apache Tomcat on Windows systems:</p>
<ul>
	<li>If you are using Apache Tomcat in <b>standalone mode</b> (i.e. you start Tomcat using <code>catalina.bat</code>), create a batch file with the following commands:
	<p style="border: 1px solid #000; background-color: #fff; padding: 4px; margin-right: 4px;"><code>
	set JAVA_HOME=C:\Programs\j2sdk1.4.1<br>
	set CATALINA_OPTS=-Dfile.encoding=ISO-8859-1<br>
	call catalina.bat run
	</code></p>
	
	<p>Enter your desired encoding if you have specific encoding requirements and adjust the path to your Java VM.
	Place the batch file where <code>catalina.bat</code> is located, usually the <code>bin/</code> subfolder of your Tomcat installation.
	Always use this file to start the Tomcat servlet container.<br>
	If you start Tomcat using the direct links that are created in the program group, <code>catalina.bat</code> is not executed.
	Do not use the program group link to start Tomcat, replace it with a link to the new batch file.
	<br>
	<b>This is the recommended setup!</b></p>
	</li>
	<li>If must use Apache Tomcat as a <b>service</b>, you have to modify the registry of your windows system:
		<ol style="margin-top:5px; margin-bottom: 5px;">
			<li>Open <code>regedit.exe</code> with the "run" option of your Windows start menu.</li>
			<li>Locate your Tomcat service under <code>HKEY_LOCAL_MACHINE/SYSTEM/CurrentControlSet/Services</code> (usually <code>Apache Tomcat</code>)
			<li>Check the <code>Parameters</code> list and create a new parameter: with the right mouse button press "New", select "String Value" and add:
			<br><b>Name:</b> JVM Option Number 3 (the number should be the next available in line)
			<br><b>Value:</b> -Dfile.encoding=ISO-8859-1 (enter your desired encoding)
			<li>Edit the key <code>JVM Option Count</code> and increase the value by 1.</li>
			<li>Reboot Windows.</li>
		</ol>
	</li>
</ul>


<p style="font-weight: bold;">Configuring Apache Tomcat on Unix/Linux systems:</p>
<ul>
	<li>Edit the configuration file <code>tomcat4.conf</code> in the <code>conf</code> folder of your Tomcat installation.
		Find the line <code>CATALINA_OPTS=" ... "</code> and add the parameter <code>-Dfile.encoding=ISO-8859-1</code>, e.g.

	<p style="border: 1px solid #000; background-color: #fff; padding: 4px; margin-right: 4px;"><code>
	CATALINA_OPTS="-Dfile.encoding=ISO-8859-1"
	</code></p>
	</li>
</ul>

</div>
</td></tr></table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td></tr></table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<form action="" method="post" class="nomargin">
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>