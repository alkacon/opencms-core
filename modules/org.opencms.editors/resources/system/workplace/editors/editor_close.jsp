<%@ page import="
	org.opencms.jsp.*, 
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*
" session="false" %><%	

	// initialize the workplace class
	CmsEditorFrameset wp = new CmsEditorFrameset(new CmsJspActionElement(pageContext, request, response));
	
	// determine the action parameter
	String action = request.getParameter(CmsDialog.PARAM_ACTION);
	
//////////////////// ACTION: delete the temporary file and unlock the resource
if (CmsEditor.EDITOR_EXIT.equals(action)) {
	wp.actionClear(true);
} else {
//////////////////// ACTION: show nothing (this frame is hidden and only used when the user presses the "Back" button or closes the window)

%><%= wp.htmlStart() %><script type="text/javascript">
<!--
function closePage(tempfile, resource) {
	this.location.href = "editor_close.html?<%= CmsDialog.PARAM_ACTION %>=<%= CmsEditor.EDITOR_EXIT %>&resource=" + resource + "&tempfile=" + tempfile;
}
//-->
</script><body></body><%= wp.htmlEnd() %>
<% } %>
