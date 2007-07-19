<%@page buffer="none" session="false" import="org.opencms.jsp.*, org.opencms.frontend.templateone.form.*, java.util.*" %><%

// Initialize JSP action element
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

CmsFormHandler formHandler = (CmsFormHandler)request.getAttribute("formhandler"); 

%><%= formHandler.getFormConfiguration().getFormConfirmationText() %>
<table border="0" style="margin-top: 14px;">
<%
List resultList = formHandler.getFormConfiguration().getFields();

for (int i = 0, n = resultList.size(); i < n; i++) {
	I_CmsField current = (I_CmsField)resultList.get(i);
	if (!CmsHiddenField.class.isAssignableFrom(current.getClass()) && !CmsPrivacyField.class.isAssignableFrom(current.getClass())) {
		out.print("<tr>\n\t<td valign=\"top\">" + current.getLabel() + "</td>");
		out.print("\n\t<td valign=\"top\" style=\"font-weight: bold;\">" + formHandler.convertToHtmlValue(current.toString()) + "</td></tr>\n");
	}
}

%>
</table>