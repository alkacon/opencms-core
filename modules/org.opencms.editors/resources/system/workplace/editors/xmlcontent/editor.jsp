<%@ page session="false" import="
	org.opencms.jsp.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*
"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsEditorFrameset wp = new CmsEditorFrameset(cms);
	
	String params = wp.allParamsAsRequest();

 %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= cms.getRequestContext().currentUser().getName() %>) - <%= wp.getParamResource() %></title>
</head>

<frameset rows="24,*" border="0" frameborder="0" framespacing="0">
    <frame src="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_buttons.jsp?" + CmsDialog.PARAM_ACTION + "=" + CmsEditor.EDITOR_SHOW + "&" + params) %>" name="buttonbar" noresize scrolling="no">
    <frame src="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp?" + params) %>" name="editform" noresize scrolling="auto">
</frameset>

</html>