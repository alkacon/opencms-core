<%@ page import="org.opencms.jsp.*, org.opencms.workplace.editors.*" buffer="none" session="false" %><%	

	// initialize the workplace class
	CmsEditorFrameset wp = new CmsEditorFrameset(new CmsJspActionElement(pageContext, request, response));
	
	// determine the action parameter
	String action = (String)request.getParameter(wp.PARAM_ACTION);
	
//////////////////// ACTION: delete the temporary file and unlock the resource
if (wp.EDITOR_EXIT.equals(action)) {
	wp.actionClear(true);
} else {
//////////////////// ACTION: show nothing (this frame is hidden and only used when the user presses the "Back" button or closes the window)

%><%= wp.htmlStart() %><script type="text/javascript">
<!--
function closePage(tempfile, resource) {
	this.location.href = "editor_close.html?<%= wp.PARAM_ACTION %>=<%= wp.EDITOR_EXIT %>&resource=" + resource + "&tempfile=" + tempfile;
}
//-->
</script><body></body><%= wp.htmlEnd() %>
<% } %>
