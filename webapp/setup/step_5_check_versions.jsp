<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="com.opencms.boot.*,java.util.*" %>

<%
    
    /* next page to be accessed */
    String nextPage = "";

    /* request parameters */
    boolean submited = (request.getParameter("systemInfo") != null); 
    boolean info = (request.getParameter("systemInfo") != null) && (request.getParameter("systemInfo").equals("false"));
    boolean accepted = (request.getParameter("accept") != null) && (request.getParameter("accept").equals("true")); 
    
    
    /* Servlet engine */
    String servletEngine = "";
    boolean supportedServletEngine = false;
    int unsupportedServletEngine = -1;
    
    /* add supported engines here */
    String[] supportedEngines = {"Apache Tomcat/4.1", "Apache Tomcat/4.0", "Apache Tomcat/5.0"};
    
    /* add unsupported enginges here */
    String[] unsupportedEngines = {"Tomcat Web Server/3.2", "Tomcat Web Server/3.3", "Resin/2.0.b2" };
    String[] unsEngMessages = {        
  		"OpenCms does not work correctly with Tomcat 3.2.x. Tomcat 3.2.x uses its own XML parser which results in major errors while using OpenCms. Please use Tomcat 4.x instead.", 
    	"Tomcat 3.3 is no longer supported. Please use Tomcat 4.x instead." , 
    	"The OpenCms JSP integration does currently not work with Resin. Please use Tomcat 4.x instead."
   	};
    
    /* JDK version */
    String requiredJDK = "1.4.0";
    String JDKVersion = "";
    boolean supportedJDK = false;
    
    
    /* true if properties are initialized */
    boolean setupOk = (Bean.getProperties()!=null);
        
    if(setupOk) {
        
        if(submited) {
            nextPage = "step_6_save_properties.jsp";
        }
        else    {
            /* checking versions */
            servletEngine = config.getServletContext().getServerInfo();
            JDKVersion = System.getProperty("java.version");
            
            CmsSetupUtils.writeVersionInfo(servletEngine, JDKVersion, config.getServletContext().getRealPath("/"));
            supportedJDK = CmsSetupUtils.compareJDKVersions(JDKVersion, requiredJDK);
            supportedServletEngine = CmsSetupUtils.supportedServletEngine(servletEngine, supportedEngines);
            unsupportedServletEngine = CmsSetupUtils.unsupportedServletEngine(servletEngine, unsupportedEngines);
        }
    }

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
    <td><form method="POST">    
        <table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
            <tr>
                <td class="title" height="25">OpenCms Setup Wizard</td>
            </tr>

            <tr>
                <td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0">&nbsp;</td>
            </tr>
            <% if(setupOk)  { %>
            <tr>
                <td height="375" align="center" valign="middle">
                    <%  if(submited) {
                            if(info && !accepted)   {
                                out.print("<b>To continue the OpenCms setup you have to recognize that your system may not work with OpenCms!");
                            }
                            else    {
                                out.print("<script language='Javascript'>document.location.href='" + nextPage + "';</script>");
                            }
                        } else { %>                 
                    <table border="0" cellpadding="5">
                        <tr><td class="bold" width="100">JDK version:</td><td width="300"><%= JDKVersion %></td><td width="30"><% if(supportedJDK)out.print("<img src='resources/check.gif'>");else out.print("<img src='resources/cross.gif'>"); %></td></tr>                                          
                        <tr><td class="bold">Servlet engine:</td><td><%= servletEngine %></td><td><% if(supportedServletEngine)out.print("<img src='resources/check.gif'>");else if (unsupportedServletEngine > -1)out.print("<img src='resources/cross.gif'>");else out.print("<img src='resources/unknown.gif'>"); %></td></tr>
                        <tr><td colspan="3" height="30">&nbsp;</td></tr>
                        <tr><td align="center" valign="bottom">
                        <%  
                            boolean red = !supportedJDK || (unsupportedServletEngine > -1);
                            boolean yellow = !supportedServletEngine;
                            
                            boolean systemOk = !(red || yellow);
                            
                            if(red) {
                                out.print("<img src='resources/traffic_red.gif'>");
                            }
                            else if (yellow)        {
                                out.print("<img src='resources/traffic_yellow.gif'>");
                            }
                            else    {
                                out.print("<img src='resources/traffic_green.gif'>");
                            }
                        %>
                        </td>
                        <td colspan="2" valign="middle">
                        <%
                            if(red) {
                                out.println("<p><b>Attention:</b> Your system does not have the necessary components to use OpenCms. It is assumed that OpenCms will not run on your system.</p>");
                                if (unsupportedServletEngine > -1) {
                                    out.println("<p>"+unsEngMessages[unsupportedServletEngine]+"</p>");
                                }
                            }
                            else if (yellow)    {
                                out.print("<b>Attention:</b> Your system uses components which have not been tested to work with OpenCms. It is possible that OpenCms will not run on your system.");
                            }
                            else    {
                                out.print("Your system uses components which have been tested to work properly with OpenCms.");
                            }
                        %></td>
                        </tr>
                        <tr><td colspan="3" height="30">&nbsp;</td></tr>                        
                        <% if(!systemOk)    { %>                        
                            <tr><td colspan="3" class="bold" align="center"><input type="checkbox" name="accept" value="true"> I have noticed that my system may not have the necessary components to use OpenCms. Continue anyway.</td></tr>
                        <% } %>                     
                    </table>
                    <input type="hidden" name="systemInfo" value="<% if(systemOk)out.print("true");else out.print("false"); %>">                                                
                    <% } %>
                </td>
            </tr>
            <tr>
                <td height="50" align="center">
                    <table border="0">
                        <tr>
                            <td width="200" align="right">
                                <input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-4)">
                            </td>
                            <td width="200" align="left">                                                                                       
                            <%  if(submited && info && !accepted)   { %>
                                <input type="button" disabled name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
                            <% } else { %>
                                <input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
                            <% } %>
                            </td>                           
                            <td width="200" align="center">
                                <input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <% } else   { %>
            <tr>
                <td align="center" valign="top">
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
</td>
</tr>
</table>
</body>
</html>