<%@page taglibs="cms" import="java.util.*,org.opencms.main.*,org.opencms.i18n.*" trimDirectiveWhitespaces="true" %><!DOCTYPE html>
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
</style>

<script type="text/javascript">
$(function() { 
    setTimeout(function() {
        window.timerId = setInterval(function() {
            scrollBy(0, 1);
            if($(window).scrollTop() + $(window).height() == $(document).height()) {
                clearInterval(window.timerId);
            }
        } , 20);
    } , 1000);
    
    $("body").click(function() {
        clearInterval(window.timerId);
    }); 
    
    
});


</script>

</head>
<body>
    <div class="center">
        <h1>This is OpenCms <%= OpenCms.getSystemInfo().getVersionNumber() %></h1>
        <p><a href="http://alkacon.com" target="_blank">&copy; Alkacon Software GmbH - All rights reserved</a></p>    
        <h2>Version and Build Information</h2>    
    </div>
    
    <table>
    <tr><td>Version:</td><td><%= OpenCms.getSystemInfo().getVersionNumber() %></td></tr>
<%
    for (String key : OpenCms.getSystemInfo().getBuildInfoKeys()) {
        CmsSystemInfo.BuildInfoItem item = OpenCms.getSystemInfo().getBuildInfoItem(key);
%>
    <tr><td><%= CmsEncoder.escapeXml(item.getNiceName())%>:</td><td><%= CmsEncoder.escapeXml(item.getValue()) %></td></tr>
<%
    // <!-- end of for - loop (!) -->
    }
%>
    </table>

    <div class="center">
        <p>
            OpenCms is free software available under the GNU LGPL license.
            Alkacon OpenCms and the OpenCms logo are registered trademarks of Alkacon Software GmbH.
        </p>    
        <p>
            Visit the OpenCms Website at <a href="http://opencms.org" target="_blank">http://opencms.org</a>
        </p>
    </div>
    
</body>
</html>