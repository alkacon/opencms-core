<%@page import="org.opencms.security.CmsRole"%>
<%@page import="org.opencms.jsp.CmsJspActionElement"%>
<%@page import="java.util.*,java.text.*,org.opencms.main.*,org.opencms.i18n.*,org.opencms.file.*"%><%@
    taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><!DOCTYPE html>
<html>
<head>
<style type="text/css">
    @font-face {
        font-family: "Open Sans";
        src: url(/VAADIN/themes/valo/fonts/open-sans/OpenSans-Regular-webfont.eot);
        src: url(/VAADIN/themes/valo/fonts/open-sans/OpenSans-Regular-webfont.eot?#iefix) format("embedded-opentype"), url(/VAADIN/themes/valo/fonts/open-sans/OpenSans-Regular-webfont.woff) format("woff"), url(/VAADIN/themes/valo/fonts/open-sans/OpenSans-Regular-webfont.ttf) format("truetype");
        font-weight: 400;
        font-style: normal;
    }
    * { font-family: "Open Sans", sans-serif; font-size: 14px; }
    body { margin: 5px 6px; }
    table { margin-left: auto; margin-right: auto; }
    h1 { font-size: 18px; }
    h1, h2, p {margin-top: 0; margin-bottom: 6px;}
    a { color:#b31b34; text-decoration:none; }
    a:hover { text-decoration:underline; }
    table tr > td:first-of-type { white-space: nowrap; color: #76767f; padding-right: 10px; }
    table td { vertical-align: top; }
    table {margin: 12px 6px;}
    .center { text-align: center; }
</style>
</head>
<body>
    <div class="center">
        <h1>This is OpenCms <%= OpenCms.getSystemInfo().getVersionNumber() %></h1>
        <p><a href="http://alkacon.com" target="_blank" rel="noopener">&copy; Alkacon Software GmbH &amp; Co. KG - All rights reserved</a></p>
        <h2>Version and Build Information</h2>
    </div>
	<% CmsObject cms = new CmsJspActionElement(pageContext, request, response).getCmsObject(); %>
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
    mailBody += "Startup time: " + (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z")).format(Long.valueOf(OpenCms.getSystemInfo().getStartupTime())) + "\r\n";
    mailBody += "Uptime: " + (new SimpleDateFormat("HH:mm:ss")).format(Long.valueOf(OpenCms.getSystemInfo().getRuntime())) + "\r\n";
    mailBody += "Default Locale: " + CmsLocaleManager.getDefaultLocale() + "\r\n";
    if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)) {
    	mailBody += "Java Runtime: " + System.getProperty("java.runtime.version") + "\r\n";
    }

    mailBody = CmsEncoder.escapeWBlanks(mailBody, "US_ASCII");
    String mailSubject = "OpenCms Version information for server: " + OpenCms.getSystemInfo().getServerName();
    mailSubject = CmsEncoder.escapeWBlanks(mailSubject, "US_ASCII");
    if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)) { %>
    <tr><td>Java Runtime:</td><td><%= System.getProperty("java.runtime.version") %></td></tr>
    <tr><td>Server:</td><td><%= application.getServerInfo() %> - Servlet/<%= application.getMajorVersion() %>.<%= application.getMinorVersion() %> - JSP/<%=JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() %></td></tr>
    <% } %>
    </table>

    <div class="center">
        <p>
            OpenCms is free software available under the GNU LGPL license.
            Alkacon OpenCms and the OpenCms logo are registered trademarks of Alkacon Software GmbH &amp; Co. KG.
        </p>
        <p>
            Visit the OpenCms Website at <a href="http://opencms.org" target="_blank" rel="noopener">http://opencms.org</a>.
        </p>
        <p>
            <a href="mailto:?subject=<%= mailSubject %>&body=<%= mailBody %>">Copy this version information to an email.</a>
        </p>
    </div>

</body>
</html>