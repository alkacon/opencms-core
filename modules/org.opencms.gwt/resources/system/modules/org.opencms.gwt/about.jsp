<%@page taglibs="cms" import="java.util.*,org.opencms.main.*,org.opencms.i18n.*" %><!DOCTYPE html>
<html>
<head>
<cms:jquery js="jquery" />
<style type="text/css">
	* { 
	font-family: Verdana,Helvetica,Arial,sans-serif;
	font-size: 12px; 
	}
	
	.license p { 
		margin-top: 0px;
		margin-bottom: 0px; 
		text-align: center; 
	}
	
	.versionline { 
		text-align: center;
		font-size: 16px; 
	}

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
	<p class="versionline">
	       OpenCms <%= OpenCms.getSystemInfo().getVersionNumber() %>
	</p>
<%
		StringBuffer html = new StringBuffer();
        html.append("<table style='margin-left:auto; margin-right: auto; margin-bottom: 10px'>");
        Set<String> keys = OpenCms.getSystemInfo().getBuildInfoKeys();
        for (String key : keys) {
            CmsSystemInfo.BuildInfoItem item = OpenCms.getSystemInfo().getBuildInfoItem(key);
            html.append("<tr>");

            html.append("<td>");
            html.append(CmsEncoder.escapeXml(item.getNiceName() + ":"));
            html.append("</td>");

            html.append("<td>");
            html.append(CmsEncoder.escapeXml(item.getValue()));
            html.append("</td>");

            html.append("</tr>");
        }
        html.append("</table>");
		out.println(html.toString());
%>
<div class="license">
	<p><a href="http://www.opencms.org">OpenCms</a> is free software available under the GNU LGPL license.</p>
	<p>Alkacon OpenCms and the OpenCms logo are registered trademarks of Alkacon Software GmbH.</p>
	<p>© 2002 - 2014 Alkacon Software GmbH. All rights reserved.</p>
</div>

</body>
</html>