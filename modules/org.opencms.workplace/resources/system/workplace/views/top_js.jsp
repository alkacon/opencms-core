<%@ page import="
    org.opencms.main.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%
	CmsExplorerInit wp = new CmsExplorerInit(new CmsJspActionElement(pageContext, request, response));
	
%>function vars_resources() { 

this.stati = new Array(
     
	"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE0_0) %>",
	"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE1_0) %>",
	"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE2_0) %>",
	"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_STATE3_0) %>"
);
this.descr = new Array(
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0) %>",  						
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0) %>",  						 
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0) %>", 
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0) %>",
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0) %>", 
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0) %>", 
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0) %>",
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0) %>",
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0) %>",
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0) %>",
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0) %>",  
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0) %>", 
		"<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0) %>"
);	    


this.actProject;        
this.onlineProject;
this.actDirectory;
this.actDirId;

this.userName="<%= wp.getSettings().getUser().getName() %>";
this.servpath="<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";
this.serverName="<%= request.getServerName() %>";

this.lockedBy="<%=    org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_TITLE_LOCKED_0) %>";
this.langback="<%=    org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_BUTTON_BACK_0) %>";
this.langsearch="<%=  org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_BUTTON_SEARCH_0) %>";
this.langup="<%=      org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_BUTTON_PARENT_0) %>";
this.langnew="<%=     org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_BUTTON_NEW_0) %>";
this.langupload="<%=  org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_BUTTON_UPLOAD_0) %>";
this.langadress="<%=  org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_ADRESS_0) %>";
this.langpage="<%=    org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_INPUT_PAGE_0) %>";
this.langloading="<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_LABEL_LOADING_0) %>";
this.altlockedby="<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDBY_0) %>";
this.altlockedin="<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_LOCKEDIN_0) %>";
this.altbelongto="<%= org.opencms.workplace.explorer.Messages.get().key(wp.getLocale(), org.opencms.workplace.explorer.Messages.GUI_EXPLORER_ALTBELONGTO_0) %>";



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