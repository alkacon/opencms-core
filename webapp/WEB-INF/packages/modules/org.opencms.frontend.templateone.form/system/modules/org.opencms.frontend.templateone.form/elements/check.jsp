<%@page buffer="none" session="false" import="org.opencms.i18n.*,org.opencms.jsp.*, org.opencms.frontend.templateone.form.*, java.util.*" %><%

// Initialize JSP action element
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

CmsFormHandler formHandler = (CmsFormHandler)request.getAttribute("formhandler");

CmsMessages messages = formHandler.getMessages(); 

%><%= formHandler.getFormConfiguration().getFormCheckText() %>

<table border="0" style="margin-top: 14px;">
<%
List resultList = formHandler.createValuesFromFields();
Iterator i = resultList.iterator();

while(i.hasNext()) {
	CmsFieldValue current = (CmsFieldValue)i.next();
	if (current.isShow()) {
		out.print("<tr>\n\t<td valign=\"top\">" + current.getLabel() + "</td>");
		out.print("\n\t<td valign=\"top\" style=\"font-weight: bold;\">" + formHandler.convertToHtmlValue(current.getValue()) + "</td></tr>\n");
	}
}

%></table>

<table border="0" style="margin-top: 14px;">
<tr>
<form name="confirmvalues" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<input type="hidden" name="<%= CmsFormHandler.C_PARAM_FORMACTION %>" value="<%= CmsFormHandler.C_ACTION_CONFIRMED %>">
<%= formHandler.createHiddenFields() %>
<td><input type="submit" value="<%= messages.key("form.button.checked") %>" class="formbutton">&nbsp;&nbsp;&nbsp;&nbsp;</td>
</form>


<form name="displayvalues" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<input type="hidden" name="<%= CmsFormHandler.C_PARAM_FORMACTION %>" value="<%= CmsFormHandler.C_ACTION_CORRECT_INPUT %>">
<%= formHandler.createHiddenFields() %>
<td><input type="submit" value="<%= messages.key("form.button.correct") %>" class="formbutton"></td>
</form>
</tr>
</table>