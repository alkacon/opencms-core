<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);


if (cms.getLayout().equals("common")) {
	if (cms.template("start")) {
		%><table border="0" cellpadding="3" cellspacing="8" style="width: 100%;">
		<%
	} else if (cms.template("column_start")) {
		%><tr>
		<%
	} else if (cms.template("element_start_1")) {
		%><td class="element" colspan="2">
		<%
	} else if (cms.template("element_start_2")) {
		%><td class="element" style="width: 50%;">
		<%
	} else if (cms.template("element_end")) {
		%></td>
		<%
	} else if (cms.template("column_end")) {
		%></tr>
		<%
	} else if (cms.template("end")) {
		%></table>
		<%
	}
} else if (cms.getLayout().equals("accessible") || cms.getLayout().equals("print")) {
	if (cms.template("element_start_1") || cms.template("element_start_2")) {
		%><div class="element">
		<%
	} else if (cms.template("element_end")) {
		%></div><div style="display: none;"><br clear="all" /></div>
		<%
	}
}
%>