<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

if (cms.getLayout().equals("common")) {
	if (cms.template("start")) {
		%><table border="0" cellpadding="3" cellspacing="8">
		<tr>
		<%
	} else if (cms.template("body_start")) {
		%><td class="oldelement" style="width: ${width}px;">
		<%
	} else if (cms.template("body_end")) {
		%></td>
		<%
	} else if (cms.template("end")) {
		%></tr>
		</table>
		<%
	}
} else if (cms.getLayout().equals("accessible") || cms.getLayout().equals("print")) {
	if (cms.template("body_start")) {
		%><div class="element">
		<%
	} else if (cms.template("body_end")) {
		%></div><div style="display: none;"><br clear="all" /></div>
		<%
	}
}
%>
