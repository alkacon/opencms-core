<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*,
	java.util.*"
	buffer="none"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsSimpleEditor wp = new CmsSimpleEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsSimpleEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsSimpleEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsSimpleEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;
case CmsSimpleEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsSimpleEditor.ACTION_CANCEL) {
		// an error occured during save
		break;
	}

case CmsSimpleEditor.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)

	%><%@ include file="mainpage.txt" %><%
	
}

%>