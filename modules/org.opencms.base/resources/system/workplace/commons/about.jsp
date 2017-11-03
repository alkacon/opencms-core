<%@page import="java.util.*,java.text.*,org.opencms.main.*,org.opencms.i18n.*"%><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><!DOCTYPE html>
<html>
<head>
<cms:jquery js="jquery" />
<style type="text/css">
    * { font-family: Verdana,Helvetica,Arial,sans-serif; font-size: 12px; }
    table { margin-left: auto; margin-right: auto; }      
    h1 { font-size: 18px; }
    a:link { color:#b31b34; text-decoration:none; }
    a:visited { color:#b31b34; text-decoration:none; }
    a:hover { color:#b31b34; text-decoration:underline; }
   .center { text-align: center; }
   body { overflow: hidden; }
</style>
</head>
<body>
    <div class="center">
        <h1>This is OpenCms <%= OpenCms.getSystemInfo().getVersionNumber() %></h1>
        <p><a href="http://alkacon.com" target="_blank">&copy; Alkacon Software GmbH &amp; Co. KG - All rights reserved</a></p>    
        <h2>Version and Build Information</h2>    
    </div>
    
    <table>
    <tr><td>Version:</td><td><%= OpenCms.getSystemInfo().getVersionNumber() %></td></tr>
<%
        String mailBody = "OpenCms Version: " + OpenCms.getSystemInfo().getVersionNumber() + "\r\n\r\n";
        for (String key : OpenCms.getSystemInfo().getBuildInfo().keySet()) {
            CmsSystemInfo.BuildInfoItem item = OpenCms.getSystemInfo().getBuildInfo().get(key);        
            mailBody += item.getNiceName() + ": " + item.getValue() + "\r\n";
%>
    <tr><td><%= CmsEncoder.escapeXml(item.getNiceName())%>:</td><td><%= CmsEncoder.escapeXml(item.getValue()) %></td></tr>
<%
    // <!-- end of for - loop (!) -->
    }

    // more system information for the email
    mailBody += "\r\nServer Name: " + OpenCms.getSystemInfo().getServerName() + "\r\n";
    mailBody += "OpenCms context: " + OpenCms.getSystemInfo().getOpenCmsContext() + "\r\n";
    mailBody += "Startup time: " + (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z")).format(OpenCms.getSystemInfo().getStartupTime()) + "\r\n";
    mailBody += "Runtime: " + (new SimpleDateFormat("HH:mm:ss")).format(OpenCms.getSystemInfo().getRuntime()) + "\r\n";
    mailBody += "Default locale: " + OpenCms.getLocaleManager().getDefaultLocale() + "\r\n";
    
    mailBody = CmsEncoder.escapeWBlanks(mailBody, "US_ASCII");
    String mailSubject = "OpenCms Version information for server: " + OpenCms.getSystemInfo().getServerName();
    mailSubject = CmsEncoder.escapeWBlanks(mailSubject, "US_ASCII");
%>
    </table>

    <div class="center">
        <p>
            OpenCms is free software available under the GNU LGPL license.
            Alkacon OpenCms and the OpenCms logo are registered trademarks of Alkacon Software GmbH &amp; Co. KG.
        </p>    
        <p>
            Visit the OpenCms Website at <a href="http://opencms.org" target="_blank">http://opencms.org</a>.
        </p>
        <p>
            <a href="mailto:?subject=<%= mailSubject %>&body=<%= mailBody %>">Copy this version information to an email.</a>
        </p>
    </div>
    
</body>
</html>