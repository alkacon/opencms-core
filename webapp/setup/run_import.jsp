<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Import packages */ %>
<%@ page import="com.opencms.boot.*,java.util.*" %>

<%
    
    /* true if properties are initialized */
    boolean setupOk = (Bean.getProperties()!=null);
    
    /* check params */
    boolean importWp = false;
    String param = request.getParameter("importWorkplace");
    if(param != null)   {
        importWp = param.equals("true");
    }

	CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
	if (!Bean.getSetupType()) {
		param = request.getParameter("directoryTranslationEnabled");
		Bean.setDirectoryTranslationEnabled( param );
			
		/* Save Properties to file "opencms.properties" and 2nd time */
		Utils.saveProperties(Bean.getProperties(),"opencms.properties",true);
	}
	// Restore the registry.xml either to or from a backup file
	Utils.backupRegistry("registry.xml", "registry.ori");							
    
    /* next page */
    String nextPage = "activex.jsp";
%>


<!-- ------------------------------------------------------------------------------------------------------------------- -->


<html>
<head> 
    <title>OpenCms Setup Wizard</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <link rel="Stylesheet" type="text/css" href="style.css">    
</head>
<% if(importWp) {   %>

<frameset rows="100%,*">
    <frame src="display_import.jsp" name="display">
    <frame src="about:blank" name="data">
</frameset> 

<%}  else   { %>
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
                <td height="50" align="right"><img src="opencms.gif" alt="OpenCms" border="0"></td>
            </tr>
            <% if(setupOk)  { %>
            <tr>
                <td align="center" valign="middle" height="375"><p>You have not imported the workplace.</p><p><b>Warning: &nbsp;&nbsp;</b>OpenCms will not work without the virtual file system!</p></td>
            </tr>

            <tr>
                <td height="50" align="center">
                    <table border="0">
                        <tr>
                            <td width="200" align="right">
                                <input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.back();">
                            </td>
                            <td width="200" align="left">
                                <input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
                            </td>
                            <td width="200" align="center">
                                <input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <% }else { %>
            <tr>
                <td align="center" valign="top" height="425">
                    <p><b>ERROR</b></p>
                    The setup wizard has not been started correctly!<br>
                    Please click <a href="" target="_top">here</a> to restart the Wizard
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

<% } %>
</html>