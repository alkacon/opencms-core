<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="org.opencms.setup.CmsSetupThread" scope="session"/>

<%	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
	
	/* next page to be accessed */
	String nextPage = "step_9_finished.jsp";
	
	/* stop possible running threads */
	Thread.stopLoggingThread();		
	Thread.stop();	
	
%>

<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="resources/style.css">
</head>
<body>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td><form action="<%= nextPage %>" method="POST">
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
				<td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			<% if(setupOk)	{ %>
			<tr>
				<td align="center" valign="top" height="375">	
<textarea style="width:600px;height:300px" cols="40" rows="20" wrap>
------------------ OpenCms 5.0 Browser configuration ------------------

The configuration instructions on this page apply when you set up a
browser to access the backoffice part of OpenCms. This is the part
where you can edit pages, create new pages, manage users etc. In
OpenCms this is called "the Workplace". 


------------- Configuration that applies to all browsers --------------

The OpenCms Workplace is a large JavaScript application that also
makes use of popup windows. Session management is done with cookies.

This means for a client browser that needs access to the workplace:

* Enable JavaScript
* Allow Popups
* Enable Cookies


--------- Configuring Mozilla / Netscape Navigator 7.x Clients --------

* A WYSIWYG editor is currently not available for Mozilla / Netscape
  Navigator Clients. OpenCms switches automatically to an HTML text
  area in which the end user can edit the content as HTML source code.


------- Optional installation of the advanced source code editor ------

If you use MS Internet Explorer on Windows, there is an optional
source code editor component available that offers some advanced
features. The installation of this editor is only neccessary if you
need a source code editor with functions like search and replace in
the source code editing mode. If you do not install this optional
component, you will work with a HTML textarea for source code editing.
In this case, you do not have to change any extra browser settings. If
you want to use the advanced editor, please follow the instructions
provided below.

The source code editor is a component developed by AY Software and
it’s called LeEdit OCX Control. You can download the shareware version
of this control from the site: http://www.aysoft.com/ledit.htm. This
control must be installed on all clients that need access to the source 
code editor functionality. If you do not install this component,
OpenCms will provide a HTML textarea for the source code input, whic 
is less convenient but also usable in general.

In order to configure your Internet Explorer for the source code
editor to work properly, open IEs "Internet options". Then do the
following:

* On the tab "Security", select "Trusted site zones" from the
  drop-down menu and click on "Add Sites" to add the URL (e.g.
  http://opencms.mycompany.com - ask your system administrator for the
  exact URL) of the zone’s OpenCms server. Deactivate the radio button
  "Require server verification (https:) for all sites in this zone".

* On the tab "Security", select "Trusted site zones" from the
  drop-down menu and click on "Settings". All control elements must be
  set to "Enable". A note on security: It is safe to use such controls
  with these settings since their use is allowed only for the "Trusted
  sites", and they remain disabled for all other web sites.
-----------------------------------------------------------------------
</textarea>
<p><input type="checkbox" name="understood" value="true"><b> I have understood how configure my browser</b></p>

</td>
			</tr>

			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.back();">
							</td>
							<td width="200" align="left">
								<input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Finish">
							</td>
							<td width="200" align="center">
								<input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<% } else	{ %>			
				<tr>
					<td align="center" valign="top" height="425">
						<p><b>ERROR</b></p>
						The setup wizard has not been started correctly!<br>
						Please click <a href="">here</a> to restart the Wizard
					</td>
				</tr>		
			<% } %>
		</form>
		</table>
	</td>
</tr>
</table>
</td></tr>
</table>
</body>
</html>