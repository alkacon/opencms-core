<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%

	CmsExplorerInit wp = new CmsExplorerInit(new CmsJspActionElement(pageContext, request, response));
 
 %>function vars_resources() { 

this.stati = new Array(
     
	"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE0_0) %>",
	"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE1_0) %>",
	"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE2_0) %>",
	"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE3_0) %>"
);
this.descr = new Array(
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0) %>",  						
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0) %>",  						 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAVTEXT_0) %>",  						 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0) %>", 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0) %>",
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0) %>", 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0) %>", 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0) %>",
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0) %>",
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0) %>",
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0) %>",
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0) %>",  
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0) %>", 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0) %>", 
		"<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0) %>"
);	    


this.actProject;        
this.onlineProject;
this.actDirectory;
this.actDirId;
this.showUpload = <%= wp.getShowFileUploadButtons() %>

this.userName="<%= wp.getUserName() %>";
this.servpath="<%= wp.getServerPath() %>";
this.serverName="<%= wp.getServerName() %>";

this.lockedBy="<%=    wp.key(org.opencms.workplace.explorer.Messages.GUI_TITLE_LOCKED_0) %>";
this.langback="<%=    wp.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_BACK_0) %>";
this.langsearch="<%=  wp.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_SEARCH_0) %>";
this.langup="<%=      wp.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_PARENT_0) %>";
this.langnew="<%=     wp.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_NEW_0) %>";
this.langupload="<%=  wp.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_UPLOAD_0) %>";
this.langadress="<%=  wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_ADRESS_0) %>";
this.langpage="<%=    wp.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PAGE_0) %>";
this.langloading="<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_LABEL_LOADING_0) %>";
this.altlockedby="<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDBY_0) %>";
this.altlockedin="<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDIN_0) %>";
this.altbelongto="<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_ALTBELONGTO_0) %>";
this.publishlock="<%= wp.key(org.opencms.workplace.explorer.Messages.GUI_PUBLISH_TOOLTIP_0)%>";


this.viewcfg=<%= wp.getExplorerSettings() %>;
this.adminResizeCount = 0;

} // vars_resources()

function initialize_resources() {

vi.iconPath="<%= wp.getResourceUri() %>";
vi.skinPath="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>";
vi.stylePath="<%= org.opencms.workplace.CmsWorkplace.getStyleUri(wp.getJsp(),"workplace.css") %>";
<%= wp.buildContextMenues() %>

} // initialize_resources()

vr = new vars_resources();
vi = new vars_index();

initialize_resources();
initHist();