<%@ page import="
    org.opencms.main.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%
	CmsExplorerInit wp = new CmsExplorerInit(new CmsJspActionElement(pageContext, request, response));
	org.opencms.i18n.CmsMessages messages = org.opencms.workplace.explorer.Messages.get().getBundle(wp.getLocale());
	
%>function vars_resources() { 

this.stati = new Array(
     
	"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE0_0) %>",
	"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE1_0) %>",
	"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE2_0) %>",
	"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE3_0) %>"
);
this.descr = new Array(
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0) %>",  						
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0) %>",  						 
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0) %>", 
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0) %>",
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0) %>", 
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0) %>", 
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0) %>",
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0) %>",
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0) %>",
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0) %>",
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0) %>",  
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0) %>", 
		"<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0) %>"
);	    


this.actProject;        
this.onlineProject;
this.actDirectory;
this.actDirId;

this.userName="<%= wp.getSettings().getUser().getName() %>";
this.servpath="<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";
this.serverName="<%= request.getServerName() %>";

this.lockedBy="<%=    messages.key(org.opencms.workplace.explorer.Messages.GUI_TITLE_LOCKED_0) %>";
this.langback="<%=    messages.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_BACK_0) %>";
this.langsearch="<%=  messages.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_SEARCH_0) %>";
this.langup="<%=      messages.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_PARENT_0) %>";
this.langnew="<%=     messages.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_NEW_0) %>";
this.langupload="<%=  messages.key(org.opencms.workplace.explorer.Messages.GUI_BUTTON_UPLOAD_0) %>";
this.langadress="<%=  messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_ADRESS_0) %>";
this.langpage="<%=    messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PAGE_0) %>";
this.langloading="<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_LABEL_LOADING_0) %>";
this.altlockedby="<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDBY_0) %>";
this.altlockedin="<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDIN_0) %>";
this.altbelongto="<%= messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_ALTBELONGTO_0) %>";



this.viewcfg=<%= wp.getExplorerSettings() %>;      

} // vars_resources()

function initialize_resources() {

vi.iconPath="<%= wp.getResourceUri() %>";
vi.skinPath="<%= wp.getSkinUri() %>";
vi.stylePath="<%= wp.getStyleUri(wp.getJsp(),"workplace.css") %>";
<%= wp.buildContextMenues() %>

} // initialize_resources()

vr = new vars_resources();
vi = new vars_index();

initialize_resources();
initHist();