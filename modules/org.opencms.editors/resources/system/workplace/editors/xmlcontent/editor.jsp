<%@ page session="false" import="
	org.opencms.jsp.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*
"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsEditorFrameset wp = new CmsEditorFrameset(cms);
	
	String params = wp.allParamsAsRequest();

 %><!DOCTYPE html>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= cms.getRequestContext().currentUser().getName() %>) - <%= wp.getParamResource() %></title>
<script type="text/javascript">

// stores the scroll target y coordinate when adding/removing or moving an element in the input form
var lastPosY = 0;
function setLastPosY(posY) {
	lastPosY = posY;
}

</script>
</head>

<frameset rows="24,*" border="0" frameborder="0" framespacing="0">
    <frame src="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_buttons.jsp?" + CmsDialog.PARAM_ACTION + "=" + CmsEditor.EDITOR_SHOW + "&" + params) %>" name="buttonbar" noresize scrolling="no">
    <frame src="<%= wp.getJsp().link(CmsEditor.PATH_EDITORS + "xmlcontent/editor_form.jsp?" + params) %>" name="editform" noresize scrolling="auto">
</frameset>

</html>